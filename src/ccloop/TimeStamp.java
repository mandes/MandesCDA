/* 
 * Copyright 2015 Alexandru Mandes.
 *
 * The code is available under a MIT License.
 *
 * Please cite: Mandes, A. (2015). Microstructure-based order placement in a 
 * continuous double auction agent based model, Algorithmic Finance 4:3-4, 
 * pp. 105-125. DOI: 10.3233/AF-150049. 
 *
 * Further reference: Cui, W. and Brabazon, A. (2012). An agent-based modeling 
 * approach to study price impact, Computational Intelligence for Financial 
 * Engineering & Economics (CIFEr), 2012 IEEE Conference on [proceedings], IEEE Press.
 */
package ccloop;

public class TimeStamp implements Comparable<TimeStamp>{    // discrete time line
    
    public int day;
    public int timeTick;    // intraday

    public TimeStamp(int dayTime, int intradayTime) {

        if ( intradayTime > Consts.TIMETICKSPERDAY ) {
            
            throw new MyException("TimeStamp.TimeStamp: intradayTime > Consts.TIMETICKSPERDAY ");
        }

        this.day = dayTime;
        this.timeTick = intradayTime;
    }

    @Override
    public int compareTo(TimeStamp ts) {
        
        final int BEFORE = -1;
        final int EQUAL = 0;
        final int AFTER = 1;

        if ( this == ts ) { 
            
            return EQUAL;
        }
        
        if ( this.day < ts.day ) {
            
            return BEFORE;  // oldest first
        }
        else {
            
            if ( this.day > ts.day ) {
            
                return AFTER;
            }
            else {  // ==
                
                if ( this.timeTick < ts.timeTick ) {
                    
                    return BEFORE;
                }
                else {
                    
                    if ( this.timeTick > ts.timeTick ) {
                        
                        return AFTER;
                    }
                    else {  // ==
                        
                        return EQUAL;
                    }
                }
            }
        }
    }
    
    /**
    * Define equality of state.
    */
    @Override
    public boolean equals(Object obj) {
        
      if (this == obj) {
          
          return true;
      }
      
      if (!(obj instanceof TimeStamp)) {
          
          return false;
      }

      return this.day == ((TimeStamp)obj).day && this.timeTick == ((TimeStamp)obj).timeTick;

    }

    @Override
    public int hashCode() {

        int hash = 5;
        hash = 79 * hash + this.day;
        hash = 79 * hash + this.timeTick;
        return hash;
    }
    
    @Override
    public String toString() {

	StringBuilder buf = new StringBuilder();

        buf.append("TimeStamp(").append(day);
        buf.append(",").append(timeTick).append(")");

        return(buf.toString());
    }
}
