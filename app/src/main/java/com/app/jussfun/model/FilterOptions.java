
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

public class FilterOptions {

    @SerializedName("age")
    private Age age = new Age();
    @SerializedName("bcamera")
    private Bcamera bCamera = new Bcamera();
    @SerializedName("gender")
    private Gender gender = new Gender();
    @SerializedName("hidemyage")
    private Hidemyage hideMyAge = new Hidemyage();
    @SerializedName("location")
    private Location location = new Location();

    public Age getAge() {
        return age;
    }

    public void setAge(Age age) {
        this.age = age;
    }

    public Bcamera getbCamera() {
        return bCamera;
    }

    public void setbCamera(Bcamera bCamera) {
        this.bCamera = bCamera;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Hidemyage getHideMyAge() {
        return hideMyAge;
    }

    public void setHideMyAge(Hidemyage hideMyAge) {
        this.hideMyAge = hideMyAge;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public class Hidemyage {

        @SerializedName("non-prime")
        private String nonPrime;
        @SerializedName("prime")
        private String prime;

        public String getNonPrime() {
            return nonPrime;
        }

        public void setNonPrime(String nonPrime) {
            this.nonPrime = nonPrime;
        }

        public String getPrime() {
            return prime;
        }

        public void setPrime(String prime) {
            this.prime = prime;
        }

    }

    public class Location {

        @SerializedName("non-prime")
        private String nonPrime;
        @SerializedName("prime")
        private String prime;

        public String getNonPrime() {
            return nonPrime;
        }

        public void setNonPrime(String nonPrime) {
            this.nonPrime = nonPrime;
        }

        public String getPrime() {
            return prime;
        }

        public void setPrime(String prime) {
            this.prime = prime;
        }

    }

    public class Gender {

        @SerializedName("non-prime")
        private String nonPrime;
        @SerializedName("prime")
        private String prime;

        public String getNonPrime() {
            return nonPrime;
        }

        public void setNonPrime(String nonPrime) {
            this.nonPrime = nonPrime;
        }

        public String getPrime() {
            return prime;
        }

        public void setPrime(String prime) {
            this.prime = prime;
        }

    }

    public class Bcamera {

        @SerializedName("non-prime")
        private String nonPrime;
        @SerializedName("prime")
        private String prime;

        public String getNonPrime() {
            return nonPrime;
        }

        public void setNonPrime(String nonPrime) {
            this.nonPrime = nonPrime;
        }

        public String getPrime() {
            return prime;
        }

        public void setPrime(String prime) {
            this.prime = prime;
        }

    }

    public class Age {

        @SerializedName("non-prime")
        private String nonPrime;
        @SerializedName("prime")
        private String prime;

        public String getNonPrime() {
            return nonPrime;
        }

        public void setNonPrime(String nonPrime) {
            this.nonPrime = nonPrime;
        }

        public String getPrime() {
            return prime;
        }

        public void setPrime(String prime) {
            this.prime = prime;
        }

    }
}
