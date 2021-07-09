# Dashchan Library

This repository contains libraries and Gradle plugins which help to create Dashchan extensions.

## Usage

Add this repository to your project as git submodule.

```sh
git submodule add https://github.com/Mishiranu/Dashchan-Library.git library
```

To include this project to extension project, add `includeBuild 'library'` to `settings.gradle`.

In addition, the plugin should be declared as Gradle dependency in `build.gradle`.

```groovy
buildscript {
    dependencies {
        classpath 'chan.library:plugins:0'
    }
}
```

## License

Libraries and plugins are available under the [GNU General Public License, version 3 or later](COPYING).

API headers are not subject to copyright.
