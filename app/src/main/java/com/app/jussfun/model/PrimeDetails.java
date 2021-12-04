
package com.app.jussfun.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PrimeDetails {

    @SerializedName("no_of_gem")
    private String noOfGem;
    @SerializedName("prime_availability")
    private String primeAvailability;
    @SerializedName("prime_benefits")
    private List<PrimeBenefit> primeBenefits;
    @SerializedName("prime_price")
    private String primePrice;

    public String getNoOfGem() {
        return noOfGem;
    }

    public void setNoOfGem(String noOfGem) {
        this.noOfGem = noOfGem;
    }

    public String getPrimeAvailability() {
        return primeAvailability;
    }

    public void setPrimeAvailability(String primeAvailability) {
        this.primeAvailability = primeAvailability;
    }

    public List<PrimeBenefit> getPrimeBenefits() {
        return primeBenefits;
    }

    public void setPrimeBenefits(List<PrimeBenefit> primeBenefits) {
        this.primeBenefits = primeBenefits;
    }

    public String getPrimePrice() {
        return primePrice;
    }

    public void setPrimePrice(String primePrice) {
        this.primePrice = primePrice;
    }

}
