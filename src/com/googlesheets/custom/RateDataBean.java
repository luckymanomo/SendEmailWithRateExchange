package com.googlesheets.custom;
import java.util.Date;

public class RateDataBean {
	@Override
	public String toString() {
		return "RateDataBean [dateTime=" + dateTime + ", buyingRate=" + buyingRate + ", sellingRate=" + sellingRate
				+ "]";
	}
	public RateDataBean(Date dateTime, double buyingRate, double sellingRate) {
		super();
		this.dateTime = dateTime;
		this.buyingRate = buyingRate;
		this.sellingRate = sellingRate;
	}
	public RateDataBean() {
		// TODO Auto-generated constructor stub
	}
	public Date dateTime;
	public double buyingRate;
	public double sellingRate;
	
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	public double getBuyingRate() {
		return buyingRate;
	}
	public void setBuyingRate(double buyingRate) {
		this.buyingRate = buyingRate;
	}
	public double getSellingRate() {
		return sellingRate;
	}
	public void setSellingRate(double sellingRate) {
		this.sellingRate = sellingRate;
	}
}
