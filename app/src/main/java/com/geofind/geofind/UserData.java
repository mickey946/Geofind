package com.geofind.geofind;

import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.GoogleAuthApiRequest;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.PlusClient;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.People;
import com.google.android.gms.plus.model.people.PersonBuffer;

/**
 * Created by Gil on 29/10/2014.
 */
public final class UserData {

    private static String _name;
    private static String _id;
    private static String _email;
    private static Person.Image _image;
    private static Person _person;
    private static boolean _connected = false;
    private static GoogleApiClient _mGoogleApiClient;

    private UserData(){

    }

    public static void init(GoogleApiClient mGoogleApiClient){
       _connected = true;
        _person = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        _name = null;
        _id = null;
        _email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        _image = null;
        _mGoogleApiClient = mGoogleApiClient;
    }

    public static boolean isConnected(){
        return _connected;
    }

    public static String getName(){
        return _name == null ? _name = _person.getDisplayName() : _name;
    }

    public static String getId(){
        return _id == null ? _id = _person.getId() : _id;
    }

    public static String getEmail(){
        return _email;
    }

    public static Person.Image getImage(){
        return _image == null ? _image = _person.getImage() : _image;
    }

    //for name, use getDisplayName()
    public static Person getPersonById(String id){

        PendingResult<People.LoadPeopleResult> res = Plus.PeopleApi.load(_mGoogleApiClient, id);
        Log.d("getPersonById: ", id);
        res.setResultCallback(new ResultCallback<People.LoadPeopleResult>() {
            @Override
            public void onResult(People.LoadPeopleResult loadPeopleResult) {
                if (loadPeopleResult.getStatus().getStatusCode() == CommonStatusCodes.SUCCESS) {
                    PersonBuffer personBuffer = loadPeopleResult.getPersonBuffer();
                    try {
                        int count = personBuffer.getCount();
                        Log.d("UserData person buffer", new Integer(count).toString());
                        for (int i = 0; i < count; i++) {
                            _person = personBuffer.get(i);

                        }
                    } finally {
                        Log.d("UserData person id 2", _person.getId());
                        personBuffer.close();
                    }
                }
                else{
                    Log.d("status: ", "failed");
                }
            }
        });
        return _person;
    }
}
