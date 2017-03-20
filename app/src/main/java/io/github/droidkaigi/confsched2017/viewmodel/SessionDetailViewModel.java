package io.github.droidkaigi.confsched2017.viewmodel;

import android.content.Context;
import android.databinding.BaseObservable;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.View;

import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import io.github.droidkaigi.confsched2017.R;
import io.github.droidkaigi.confsched2017.model.Session;
import io.github.droidkaigi.confsched2017.repository.sessions.MySessionsRepository;
import io.github.droidkaigi.confsched2017.repository.sessions.SessionsRepository;
import io.github.droidkaigi.confsched2017.util.AlarmUtil;
import io.github.droidkaigi.confsched2017.util.DateUtil;
import io.github.droidkaigi.confsched2017.util.LocaleUtil;
import io.github.droidkaigi.confsched2017.view.helper.Navigator;
import io.reactivex.Completable;
import timber.log.Timber;

public class SessionDetailViewModel extends BaseObservable implements ViewModel {

    private static final String TAG = SessionDetailViewModel.class.getSimpleName();

    private final Context context;

    private final Navigator navigator;

    private final SessionsRepository sessionsRepository;

    private final MySessionsRepository mySessionsRepository;

    private String sessionTitle;

    private String speakerImageUrl;

    @ColorRes
    private int sessionVividColorResId = R.color.white;

    @ColorRes
    private int sessionPaleColorResId = R.color.white;

    @StyleRes
    private int sessionThemeResId = R.color.white;

    @StringRes
    private int languageResId = R.string.lang_en;

    private String sessionTimeRange;

    public Session session;

    private boolean isMySession;

    private int tagContainerVisibility;

    private int speakerVisibility;

    private int slideIconVisibility;

    private int dashVideoIconVisibility;

    private int roomVisibility;

    private int topicVisibility;

    private int feedbackButtonVisiblity;

    private Callback callback;

    @Inject
    public SessionDetailViewModel(Context context, Navigator navigator, SessionsRepository sessionsRepository,
            MySessionsRepository mySessionsRepository) {
        this.context = context;
        this.navigator = navigator;
        this.sessionsRepository = sessionsRepository;
        this.mySessionsRepository = mySessionsRepository;
    }

    private void setSession(@NonNull Session session) {
        this.session = session;
        this.sessionTitle = session.getTitle();

        if (session.getSpeaker() != null) {
            this.speakerImageUrl = session.getSpeaker().getAdjustedImageUrl();
        }
        TopicColor topicColor = TopicColor.from(session.getTopic());
        this.sessionVividColorResId = topicColor.vividColorResId;
        this.sessionPaleColorResId = topicColor.paleColorResId;
        this.sessionThemeResId = topicColor.themeId;
        this.sessionTimeRange = decideSessionTimeRange(context, session);
        this.isMySession = mySessionsRepository.isExist(session.getId());
        this.tagContainerVisibility = !session.isDinner() ? View.VISIBLE : View.GONE;
        this.speakerVisibility = !session.isDinner() ? View.VISIBLE : View.GONE;
        this.slideIconVisibility = session.getSlideUrl() != null ? View.VISIBLE : View.GONE;
        this.dashVideoIconVisibility = session.getMovieUrl() != null && session.getMovieDashUrl() != null ? View.VISIBLE : View.GONE;
        this.roomVisibility = session.getRoom() != null ? View.VISIBLE : View.GONE;
        this.topicVisibility = session.getTopic() != null ? View.VISIBLE : View.GONE;
        this.feedbackButtonVisiblity = !session.isDinner() ? View.VISIBLE : View.GONE;
        this.languageResId = session.getLang() != null ? decideLanguageResId(new Locale(session.getLang().toLowerCase()))
                : R.string.lang_en;
    }

    public Completable loadSession(int sessionId) {
        return sessionsRepository.find(sessionId, Locale.getDefault())
                .flatMapCompletable(session -> {
                    setSession(session);
                    return Completable.complete();
                });
    }

    private int decideLanguageResId(@NonNull Locale locale) {
        if (locale.equals(Locale.JAPANESE)) {
            return R.string.lang_ja;
        } else {
            return R.string.lang_en;
        }
    }

    @Override
    public void destroy() {
        this.callback = null;
    }

    public boolean shouldShowShareMenuItem() {
        return !TextUtils.isEmpty(session.getShareUrl());
    }

    public void onClickShareMenuItem() {
        //
    }

    public void onClickFeedbackButton(@SuppressWarnings("unused") View view) {
        navigator.navigateToFeedbackPage(session);
    }

    public void onClickSlideIcon(@SuppressWarnings("unused") View view) {
//        if (session.hasSlide()) {
//        }
    }

    public void onClickMovieIcon(@SuppressWarnings("unused") View view) {
//        if (session.hasDashVideo()) {
//        }
    }

    public void onClickFab(@SuppressWarnings("unused") View view) {
        boolean selected = true;
        if (mySessionsRepository.isExist(session.getId())) {
            selected = false;
            mySessionsRepository.delete(session)
                    .subscribe((result) -> Timber.tag(TAG).d("Deleted my session"),
                            throwable -> Timber.tag(TAG).e(throwable, "Failed to delete my session"));
            AlarmUtil.unregisterAlarm(context, session);
        } else {
            selected = true;
            mySessionsRepository.save(session)
                    .subscribe(() -> Timber.tag(TAG).d("Saved my session"),
                            throwable -> Timber.tag(TAG).e(throwable, "Failed to save my session"));
            AlarmUtil.registerAlarm(context, session);
        }

        if (callback != null) {
            callback.onClickFab(selected);
        }
    }

    public void onOverScroll() {
        if (callback != null) {
            callback.onOverScroll();
        }
    }

    private String decideSessionTimeRange(Context context, Session session) {
        Date displaySTime = LocaleUtil.getDisplayDate(session.getStime(), context);
        Date displayETime = LocaleUtil.getDisplayDate(session.getEtime(), context);

        return context.getString(R.string.session_time_range,
                DateUtil.getLongFormatDate(displaySTime),
                DateUtil.getHourMinute(displayETime),
                DateUtil.getMinutes(displaySTime, displayETime));
    }

    public String getSessionTitle() {
        return sessionTitle;
    }

    public String getSpeakerImageUrl() {
        return speakerImageUrl;
    }

    public int getSessionVividColorResId() {
        return sessionVividColorResId;
    }

    public int getSessionPaleColorResId() {
        return sessionPaleColorResId;
    }

    public int getTopicThemeResId() {
        return sessionThemeResId;
    }

    public int getLanguageResId() {
        return languageResId;
    }

    public String getSessionTimeRange() {
        return sessionTimeRange;
    }

    public boolean isMySession() {
        return isMySession;
    }

    public int getTagContainerVisibility() {
        return tagContainerVisibility;
    }

    public int getSpeakerVisibility() {
        return speakerVisibility;
    }

    public int getSlideIconVisibility() {
        return slideIconVisibility;
    }

    public int getDashVideoIconVisibility() {
        return dashVideoIconVisibility;
    }

    public int getTopicVisibility() {
        return topicVisibility;
    }

    public int getRoomVisibility() {
        return roomVisibility;
    }

    public int getFeedbackButtonVisiblity() {
        return feedbackButtonVisiblity;
    }

    public void setCallback(@NonNull Callback callback) {
        this.callback = callback;
    }

    public interface Callback {

        void onClickFab(boolean selected);

        void onOverScroll();
    }
}
