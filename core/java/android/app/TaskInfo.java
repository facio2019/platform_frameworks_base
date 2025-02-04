/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.TestApi;
import android.compat.annotation.UnsupportedAppUsage;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;
import android.window.WindowContainerToken;

/**
 * Stores information about a particular Task.
 */
public class TaskInfo {
    private static final String TAG = "TaskInfo";

    /**
     * The id of the user the task was running as.
     * @hide
     */
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    public int userId;

    /**
     * The id of the ActivityStack that currently contains this task.
     * @hide
     */
    @UnsupportedAppUsage
    public int stackId;

    /**
     * The identifier for this task.
     */
    public int taskId;

    /**
     * Whether or not this task has any running activities.
     */
    public boolean isRunning;

    /**
     * The base intent of the task (generally the intent that launched the task). This intent can
     * be used to relaunch the task (if it is no longer running) or brought to the front if it is.
     */
    @NonNull
    public Intent baseIntent;

    /**
     * The component of the first activity in the task, can be considered the "application" of this
     * task.
     */
    @Nullable
    public ComponentName baseActivity;

    /**
     * The component of the top activity in the task, currently showing to the user.
     */
    @Nullable
    public ComponentName topActivity;

    /**
     * The component of the target activity if this task was started from an activity alias.
     * Otherwise, this is null.
     */
    @Nullable
    public ComponentName origActivity;

    /**
     * The component of the activity that started this task (may be the component of the activity
     * alias).
     * @hide
     */
    @Nullable
    public ComponentName realActivity;

    /**
     * The number of activities in this task (including running).
     */
    public int numActivities;

    /**
     * The last time this task was active since boot (including time spent in sleep).
     * @hide
     */
    @UnsupportedAppUsage
    public long lastActiveTime;

    /**
     * The id of the display this task is associated with.
     * @hide
     */
    public int displayId;

    /**
     * The recent activity values for the highest activity in the stack to have set the values.
     * {@link Activity#setTaskDescription(android.app.ActivityManager.TaskDescription)}.
     */
    @Nullable
    public ActivityManager.TaskDescription taskDescription;

    /**
     * True if the task can go in the split-screen primary stack.
     * @hide
     */
    @UnsupportedAppUsage
    public boolean supportsSplitScreenMultiWindow;

    /**
     * The resize mode of the task. See {@link ActivityInfo#resizeMode}.
     * @hide
     */
    @UnsupportedAppUsage
    public int resizeMode;

    /**
     * The current configuration of the task.
     * @hide
     */
    @NonNull
    @UnsupportedAppUsage
    public final Configuration configuration = new Configuration();

    /**
     * Used as an opaque identifier for this task.
     * @hide
     */
    @NonNull
    public WindowContainerToken token;

    /**
     * The PictureInPictureParams for the Task, if set.
     * @hide
     */
    @Nullable
    public PictureInPictureParams pictureInPictureParams;

    /**
     * The activity type of the top activity in this task.
     * @hide
     */
    public @WindowConfiguration.ActivityType int topActivityType;

    /**
     * The {@link ActivityInfo} of the top activity in this task.
     * @hide
     */
    @Nullable
    public ActivityInfo topActivityInfo;

    /**
     * Whether this task is resizable. Unlike {@link #resizeMode} (which is what the top activity
     * supports), this is what the system actually uses for resizability based on other policy and
     * developer options.
     * @hide
     */
    public boolean isResizeable;

    TaskInfo() {
        // Do nothing
    }

    private TaskInfo(Parcel source) {
        readFromParcel(source);
    }

    /**
     * @param isLowResolution
     * @return
     * @hide
     */
    public ActivityManager.TaskSnapshot getTaskSnapshot(boolean isLowResolution) {
        try {
            return ActivityManager.getService().getTaskSnapshot(taskId, isLowResolution);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to get task snapshot, taskId=" + taskId, e);
            return null;
        }
    }

    /** @hide */
    @NonNull
    @TestApi
    public WindowContainerToken getToken() {
        return token;
    }

    /** @hide */
    @NonNull
    @TestApi
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Reads the TaskInfo from a parcel.
     */
    void readFromParcel(Parcel source) {
        userId = source.readInt();
        stackId = source.readInt();
        taskId = source.readInt();
        displayId = source.readInt();
        isRunning = source.readBoolean();
        baseIntent = source.readInt() != 0
                ? Intent.CREATOR.createFromParcel(source)
                : null;
        baseActivity = ComponentName.readFromParcel(source);
        topActivity = ComponentName.readFromParcel(source);
        origActivity = ComponentName.readFromParcel(source);
        realActivity = ComponentName.readFromParcel(source);

        numActivities = source.readInt();
        lastActiveTime = source.readLong();

        taskDescription = source.readInt() != 0
                ? ActivityManager.TaskDescription.CREATOR.createFromParcel(source)
                : null;
        supportsSplitScreenMultiWindow = source.readBoolean();
        resizeMode = source.readInt();
        configuration.readFromParcel(source);
        token = WindowContainerToken.CREATOR.createFromParcel(source);
        topActivityType = source.readInt();
        pictureInPictureParams = source.readInt() != 0
                ? PictureInPictureParams.CREATOR.createFromParcel(source)
                : null;
        topActivityInfo = source.readInt() != 0
                ? ActivityInfo.CREATOR.createFromParcel(source)
                : null;
        isResizeable = source.readBoolean();
    }

    /**
     * Writes the TaskInfo to a parcel.
     */
    void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(userId);
        dest.writeInt(stackId);
        dest.writeInt(taskId);
        dest.writeInt(displayId);
        dest.writeBoolean(isRunning);

        if (baseIntent != null) {
            dest.writeInt(1);
            baseIntent.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        ComponentName.writeToParcel(baseActivity, dest);
        ComponentName.writeToParcel(topActivity, dest);
        ComponentName.writeToParcel(origActivity, dest);
        ComponentName.writeToParcel(realActivity, dest);

        dest.writeInt(numActivities);
        dest.writeLong(lastActiveTime);

        if (taskDescription != null) {
            dest.writeInt(1);
            taskDescription.writeToParcel(dest, flags);
        } else {
            dest.writeInt(0);
        }
        dest.writeBoolean(supportsSplitScreenMultiWindow);
        dest.writeInt(resizeMode);
        configuration.writeToParcel(dest, flags);
        token.writeToParcel(dest, flags);
        dest.writeInt(topActivityType);
        if (pictureInPictureParams == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            pictureInPictureParams.writeToParcel(dest, flags);
        }
        if (topActivityInfo == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(1);
            topActivityInfo.writeToParcel(dest, flags);
        }
        dest.writeBoolean(isResizeable);
    }

    @Override
    public String toString() {
        return "TaskInfo{userId=" + userId + " stackId=" + stackId + " taskId=" + taskId
                + " displayId=" + displayId
                + " isRunning=" + isRunning
                + " baseIntent=" + baseIntent + " baseActivity=" + baseActivity
                + " topActivity=" + topActivity + " origActivity=" + origActivity
                + " realActivity=" + realActivity
                + " numActivities=" + numActivities
                + " lastActiveTime=" + lastActiveTime
                + " supportsSplitScreenMultiWindow=" + supportsSplitScreenMultiWindow
                + " resizeMode=" + resizeMode
                + " isResizeable=" + isResizeable
                + " token=" + token
                + " topActivityType=" + topActivityType
                + " pictureInPictureParams=" + pictureInPictureParams
                + " topActivityInfo=" + topActivityInfo;
    }
}
