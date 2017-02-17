package nicolasquartieri.com.ar.flickr_app;

//import static android.support.test.espresso.Espresso.onView;
//import static android.support.test.espresso.action.ViewActions.click;
//import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static android.support.test.espresso.action.ViewActions.typeText;
//import static android.support.test.espresso.assertion.ViewAssertions.matches;
//import static android.support.test.espresso.matcher.ViewMatchers.withId;
//import static android.support.test.espresso.matcher.ViewMatchers.withText;

import android.app.ActivityManager;
import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import ar.com.nicolasquartieri.MainActivity;
import ar.com.nicolasquartieri.R;
import ar.com.nicolasquartieri.remote.FlickrSearchApiService;

@RunWith(AndroidJUnit4.class)
public class AndroidUITest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
            MainActivity.class);

    private SearchServiceIdlingResource searchServiceIdlingResource;

    private String textToCompare;

    @Before
    public void before() {
        textToCompare = "paris";
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Context ctx = instrumentation.getTargetContext();
        // Idle Resource Register. (Not use for now)
//        searchServiceIdlingResource = new SearchServiceIdlingResource(ctx);
//        Espresso.registerIdlingResources(searchServiceIdlingResource);
    }

    @After
    public void after() {
        // Idle Resource Unregister.
//        Espresso.unregisterIdlingResources(searchServiceIdlingResource);
    }

    @Test
    public void searchEditTextWorks() {
        // Type text inside the SearchView & close the SoftKeyboard.
        Espresso.onView(ViewMatchers.withId(android.support.design.R.id.search_src_text))
                .perform(ViewActions.typeText(textToCompare), ViewActions.closeSoftKeyboard());

        // Check if the typed text is the same.
        Espresso.onView(ViewMatchers.withId(android.support.design.R.id.search_src_text))
                .check(ViewAssertions.matches(ViewMatchers.withText(textToCompare)));
    }

    @Test
    public void checkListItemVisible() {
        // Click over the first item of the list.
        Espresso.onView(ViewMatchers.withId(R.id.photo_list))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void checkFlickrSearchApiServiceDone() {
		// Type text inside the SearchView, close the SoftKeyboard & submit the query.
		Espresso.onView(ViewMatchers.withId(android.support.design.R.id.search_src_text))
				.perform(ViewActions.typeText(textToCompare),
						ViewActions.closeSoftKeyboard(),
						ViewActions.pressImeActionButton());

        // Check if the amount of elements is al least 3.
        Espresso.onView(ViewMatchers.withId(R.id.photo_list))
                .check(RecyclerViewAssertions.hasItemsCountAtLeast(3));
    }


    private class SearchServiceIdlingResource implements IdlingResource {

        private final Context context;
        private ResourceCallback callback;

        public SearchServiceIdlingResource(Context ctx) {
            context = ctx;
        }

        @Override
        public String getName() {
            return SearchServiceIdlingResource.class.getName();
        }

        @Override
        public boolean isIdleNow() {
            boolean idle = !isServiceIntentRunning();
            if (idle && callback != null) {
                callback.onTransitionToIdle();
            }
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(ResourceCallback callback) {
            this.callback = callback;
        }

        private boolean isServiceIntentRunning() {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            // Get all running services.
			List<ActivityManager.RunningServiceInfo> runningServices = am
					.getRunningServices(Integer.MAX_VALUE);
            // Check if our is running
            for (ActivityManager.RunningServiceInfo runningInfo : runningServices) {
                // TODO: Use Generics.
                if (FlickrSearchApiService.class.getName().equals(runningInfo.service.getClassName())) {
                    return true;
                }
            }
            return false;
        }
    }
}
