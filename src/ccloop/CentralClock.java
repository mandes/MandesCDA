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

public class CentralClock {    // 'world' time synchronizer
    
    private TimeStamp curTime;

    public int timeTicksPerDay; // intraday time flows from 0/1 to Consts.TIMETICKSPERDAY

    public CentralClock() {

        this.curTime = new TimeStamp(0, 0);
        this.timeTicksPerDay = Consts.TIMETICKSPERDAY;
    }

    public TimeStamp getCurTime() {
        
        return curTime;
    }

    public void updateTime(TimeStamp ts) {
        
        if ( ts.day < curTime.day 
                || ( ts.day == curTime.day && ts.timeTick < curTime.timeTick ) ) {
            
            throw new MyException("Clock.updateTime: time does not flow backwards.");
        }

        curTime = ts;
    }
    
    public TimeStamp addTime( TimeStamp initTime, int timeSkip ) {

        // timeskip is 0 in case of adding latency

        // max = 3
        // 1. (3,1) + 7 = (5,2)
        // 2. (3,1) - 5 = (1,2)
        
        int day = initTime.day;
        int timeTick = initTime.timeTick + timeSkip;    // 1. 1+7=8; 2. 1-5=-4
        
        if ( timeTick > timeTicksPerDay ) {    // 1. 8 > 3

            int multiple = timeTick / timeTicksPerDay;  // 2
                    
            day += multiple;    // 3+2=5
            timeTick -= multiple * timeTicksPerDay; // 8-2*3=2
        }
        
        if ( timeTick <= 0 ) {   // 2. -4 < 0
            
            int multiple = timeTick / timeTicksPerDay;  // -1
            
            day += (multiple - 1);    // 3-1=2
            timeTick += timeTicksPerDay * (1 - multiple); // -4 + 3 * 2 = 2
        }

        return new TimeStamp( day, timeTick );
    }
}
