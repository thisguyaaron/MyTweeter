package edu.byu.cs.tweeter.client.model.service.backgroundTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.Serializable;
import java.util.List;

import edu.byu.cs.tweeter.client.model.net.ServerFacade;
import edu.byu.cs.tweeter.client.model.service.StatusService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.model.net.service.request.GetFeedRequest;
import edu.byu.cs.tweeter.model.net.service.response.GetFeedResponse;
import edu.byu.cs.tweeter.util.Pair;

/**
 * Background task that retrieves a page of statuses from a user's feed.
 */
public class GetFeedTask extends BackgroundTask {
    private static final String LOG_TAG = "GetFeedTask";
    public static final String STATUSES_KEY = "statuses";
    public static final String MORE_PAGES_KEY = "more-pages";

    private ServerFacade serverFacade;

    /**
     * Auth token for logged-in user.
     */
    private AuthToken authToken;
    /**
     * The user whose feed is being retrieved.
     * (This can be any user, not just the currently logged-in user.)
     */
    private User targetUser;
    /**
     * Maximum number of statuses to return (i.e., page size).
     */
    private int limit;
    /**
     * The last status returned in the previous page of results (can be null).
     * This allows the new page to begin where the previous page ended.
     */
    protected Status lastStatus;


    private List<Status> statuses;
    private boolean hasMorePages;
    /**
     * Message handler that will receive task results.
     */

    public GetFeedTask(StatusService service, AuthToken authToken, User targetUser, int limit, Status lastStatus,
                       Handler messageHandler) {
        super(messageHandler);
        this.authToken = authToken;
        this.targetUser = targetUser;
        this.limit = limit;
        this.lastStatus = lastStatus;
    }


    @Override
    protected void runTask() {
        try {
            GetFeedRequest request = new GetFeedRequest(authToken, targetUser, limit, lastStatus);
            GetFeedResponse response = getServerFacade().getFeed(request, StatusService.GET_FEED_URL_PATH);


            if (response.isSuccess()) {
                this.hasMorePages = response.getHasMorePages();
                this.statuses = response.getStatuses();
                sendSuccessMessage();

            }else{
                sendFailedMessage(response.getMessage());
            }

        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            sendExceptionMessage(ex);
        }

    }



    protected void loadSuccessBundle(Bundle msgBundle) {
        msgBundle.putSerializable(STATUSES_KEY, (Serializable) this.statuses);
        msgBundle.putBoolean(MORE_PAGES_KEY, this.hasMorePages);
    }



    public ServerFacade getServerFacade() {
        if(serverFacade == null) {
            serverFacade = new ServerFacade();
        }

        return new ServerFacade();
    }
}
