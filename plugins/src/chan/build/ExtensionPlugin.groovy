package chan.build

import org.gradle.api.Plugin
import org.gradle.api.Project

class ExtensionPlugin implements Plugin<Project> {
	@SuppressWarnings('unused')
	private final ExtensionPlugin extensionPlugin = null

	@Override
	void apply(Project project) {
		project.extensions.add('chan', ChanExtension)

		def chanProperties = new Properties()
		try {
			project.rootProject.file('chan.properties').newInputStream()
					.withCloseable { chanProperties.load(it) }
		} catch (IOException ignore) {
			// Ignore
		}

		project.afterEvaluate {
			ChanExtension chan = project.extensions.getByName('chan')
			def chanName = chan.name ?: project.name.replace('-', '')
			def chanNameUpper = chan.nameUpper ?: chanName.take(1).toUpperCase() + chanName.substring(1)
			def packageName = chan.packageName
					?: chanProperties['package.prefix'].with { it != null ? "$it.$chanName" : null }
					?: PluginUtils.throwKeyNotFound('packageName')
			def versionName = chan.versionName ?: PluginUtils.throwKeyNotFound('versionName')
			def apiVersion = chan.apiVersion ?: PluginUtils.throwKeyNotFound('apiVersion')
			def icon = chan.icon ?: "ic_custom_$chanName"
			def updateUri = chan.updateUri ?: chanProperties['update.uri']
			def hosts = chan.hosts ?: PluginUtils.throwKeyNotFound('hosts')
			def customUriHandler = chan.customUriHandler ?: false
			def customFilter = chan.customFilter
			def chanTitle = hosts[0]

			def requiredClasses = ['ChanConfiguration', 'ChanLocator', 'ChanMarkup', 'ChanPerformer']
					.collect { "$packageName.$chanNameUpper$it" }

			def xml = '<?xml version="1.0" encoding="utf-8"?>\n' +
					'<manifest xmlns:android="http://schemas.android.com/apk/res/android" ' +
					"package=\"$packageName\" android:versionCode=\"1\" android:versionName=\"$versionName\">\n"
			xml += '<uses-feature android:name="chan.extension" />\n'
			xml += '<application android:icon="@null" android:allowBackup="false" ' +
					"android:label=\"Dashchan for ${chanTitle}\">\n"
			xml += "<meta-data android:name=\"chan.extension.name\" android:value=\"$chanName\" />\n"
			xml += "<meta-data android:name=\"chan.extension.title\" android:value=\"${chanTitle}\" />\n"
			xml += "<meta-data android:name=\"chan.extension.version\" android:value=\"$apiVersion\" />\n"
			xml += "<meta-data android:name=\"chan.extension.icon\" android:resource=\"@drawable/$icon\" />\n"
			if (updateUri != null) {
				xml += "<meta-data android:name=\"chan.extension.source\" android:value=\"$updateUri\" />\n"
			}
			xml += '<meta-data android:name="chan.extension.class.configuration" ' +
					"android:value=\".${chanNameUpper}ChanConfiguration\" />\n"
			xml += '<meta-data android:name="chan.extension.class.performer" ' +
					"android:value=\".${chanNameUpper}ChanPerformer\" />\n"
			xml += '<meta-data android:name="chan.extension.class.locator" ' +
					"android:value=\".${chanNameUpper}ChanLocator\" />\n"
			xml += '<meta-data android:name="chan.extension.class.markup" ' +
					"android:value=\".${chanNameUpper}ChanMarkup\" />\n"
			xml += '<activity android:name="chan.application.UriHandlerActivity" ' +
					'android:label="Dashchan" android:theme="@android:style/Theme.NoDisplay">\n'
			xml += '<intent-filter>\n' +
					'<action android:name="android.intent.action.VIEW" />\n' +
					'<category android:name="android.intent.category.DEFAULT" />\n' +
					'<category android:name="android.intent.category.BROWSABLE" />\n'
			if (customFilter != null) {
				xml += customFilter
			} else {
				xml += '<data android:scheme="http" />\n' +
						'<data android:scheme="https" />\n'
				for (host in hosts) {
					xml += "<data android:host=\"$host\" />\n"
				}
			}
			xml += '</intent-filter>\n</activity>\n</application>\n</manifest>\n'
			PluginUtils.configureManifest(project, xml)

			def proguard = '-dontobfuscate\n' +
					requiredClasses.collect { "-keep class $it { *; }\n" }.join()
			def proguardFile = new File(project.buildDir, 'generated/proguard-rules.pro')
			project.preBuild.dependsOn(project.task('generateProguard', type: GenerateFileTask) {
				inputText = proguard
				outputFile = proguardFile
			})

			project.archivesBaseName = "Dashchan$chanNameUpper"

			project.android {
				buildTypes.all {
					minifyEnabled false
				}
			}

			project.android {
				defaultConfig {
					// Don't warn about unused classes
					buildConfigField 'Class[]', 'USED_CLASSES',
							'{' + requiredClasses.collect { "${it}.class" }.join(', ') + '}'
				}

				buildTypes {
					debug {
						minifyEnabled false
					}
					release {
						minifyEnabled true
					}
					all {
						crunchPngs false
						proguardFiles getDefaultProguardFile('proguard-android.txt'), proguardFile
					}
				}
			}

			project.dependencies {
				compileOnly 'chan.library:api:0'
				if (!customUriHandler) {
					implementation 'chan.library:uri-handler:0'
				}
			}
		}

		project.apply plugin: 'com.android.application'
		PluginUtils.configureProject(project)

		project.android {
			def keystoreProperties = new Properties()
			try {
				project.rootProject.file('keystore.properties').newInputStream()
						.withCloseable { keystoreProperties.load(it) }
			} catch (IOException ignore) {
				// Ignore
			}
			if (!keystoreProperties.keySet().isEmpty()) {
				signingConfigs.create('general') {
					storeFile project.rootProject.file(keystoreProperties['store.file'])
					storePassword keystoreProperties['store.password']
					keyAlias keystoreProperties['key.alias']
					keyPassword keystoreProperties['key.password']
				}

				buildTypes.debug.signingConfig signingConfigs.general
				buildTypes.release.signingConfig signingConfigs.general
			}

			lintOptions {
				abortOnError false
				disable 'MissingTranslation'
			}
		}
	}
}
