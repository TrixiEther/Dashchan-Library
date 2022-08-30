package chan.content.model;

import chan.library.api.BuildConfig;

/**
 * <p>Model containing votes data.</p>
 *
 * <p>How many votes were given for a post:</p>
 *
 * <ul>
 * <li>{@link Vote#getLikes()}</li>
 * <li>{@link Vote#getDislikes()}</li>
 * <li>{@link Vote#isShowVotes()}</li>
 * </ul>
 */
public final class Vote {

    /**
     * <p>Returns likes on post.</p>
     *
     * @return Likes count.
     */
    public int getLikes() {
        return BuildConfig.Private.expr();
    }

    /**
     * <p>Returns dislikes on post.</p>
     *
     * @return Dislikes count.
     */
    public int getDislikes() {
        return BuildConfig.Private.expr();
    }

    /**
     * <p>Returns whether the voting feature is enabled for the post.</p>
     *
     * @return Is voting active.
     */
    public boolean isShowVotes() {
        return BuildConfig.Private.expr();
    }

}