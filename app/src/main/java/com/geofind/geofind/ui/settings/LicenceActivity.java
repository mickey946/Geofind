package com.geofind.geofind.ui.settings;

import android.content.DialogInterface;
import android.os.Bundle;

import com.geofind.geofind.R;
import com.geofind.geofind.playutils.BaseGameActivity;

import de.psdev.licensesdialog.LicensesDialogFragment;
import de.psdev.licensesdialog.licenses.ApacheSoftwareLicense20;
import de.psdev.licensesdialog.licenses.CreativeCommonsAttributionNoDerivs30Unported;
import de.psdev.licensesdialog.licenses.MITLicense;
import de.psdev.licensesdialog.model.Notice;
import de.psdev.licensesdialog.model.Notices;

/**
 * An {@link android.app.Activity} that shows a
 * {@link de.psdev.licensesdialog.LicensesDialogFragment} with all the open source licences used in
 * this project.
 */
public class LicenceActivity extends BaseGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_licence);

        final Notices notices = new Notices();
        notices.addNotice(new Notice(
                "Android Compatibility Library v4",
                "https://source.android.com/",
                "Copyright (c) 2005-2011 The Android Open Source Project",
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(
                "Android Compatibility Library v7",
                "https://source.android.com/",
                "Copyright (c) 2005-2011 The Android Open Source Project",
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(
                "BaseGameUtils library",
                "https://github.com/playgameservices/android-basic-samples/tree/master/BasicSamples/libraries/BaseGameUtils",
                "Copyright (C) 2013 Google Inc.",
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(
                "PolyUtil.java",
                "https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java",
                "Copyright (C) 2013 Google Inc.",
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(
                "Android Sliding Up Panel",
                "https://github.com/umano/AndroidSlidingUpPanel",
                "Copyright © 2014 SoThree, Inc.",
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(
                "FloatingActionButton",
                "https://github.com/makovkastar/FloatingActionButton",
                "Copyright (c) 2014 Oleksandr Melnykov",
                new MITLicense()));

        notices.addNotice(new Notice(
                "PhotoView",
                "https://github.com/chrisbanes/PhotoView",
                "Copyright 2011, 2012 Chris Banes",
                new ApacheSoftwareLicense20()));

        notices.addNotice(new Notice(
                "Material Design Icons",
                "http://www.google.com/design/spec/material-design",
                "",
                new CreativeCommonsAttributionNoDerivs30Unported()));

        notices.addNotice(new Notice(
                "Sailor Lake (Blurred)",
                "http://www.jeffpang.net/",
                "Copyright 2014 Jeffrey Pang",
                new CreativeCommonsAttributionNoDerivs30Unported()));

        // the licence of this very dialog is already included.

        // TODO add Maps licences

        final LicensesDialogFragment fragment = LicensesDialogFragment.newInstance(
                notices, false, true);

        // cancel the activity when the dialog is canceled
        fragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });

        fragment.show(getSupportFragmentManager(), null);
    }

    @Override
    public void onSignInFailed() {

    }

    @Override
    public void onSignInSucceeded() {

    }
}
