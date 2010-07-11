package edu.stanford.cs.sujogger.util;

public class Track {
  public long id;
  public boolean visible;
  
  public Track(long id) {
	  this.id = id;
	  this.visible = true;
  }
  public Track(long id, boolean visible) {
	  this.id = id;
	  this.visible = visible;
  }
}
