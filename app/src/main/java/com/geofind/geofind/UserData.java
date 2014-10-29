package com.geofind.geofind;

import com.google.android.gms.plus.model.people.Person;

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

    private UserData(){

    }

    public static void init(Person person, String email){
       _connected = true;
        _person = person;
        _name = null;
        _id = null;
        _email = email;
        _image = null;
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
}
