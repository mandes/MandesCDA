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
package abmlob.agents;

import abmlob.events.Event;
import ccloop.MarketState;
import ccloop.MyException;
import ccloop.RandNumGen;

public abstract class Trader extends Agent {
    
    public int timeFrame;  // frequency, waiting time
    public double riskAversion;
    public int latency;    // priority + agents might see a not real-time, delayed state of the market
    
    public Trader( MarketState state, int timeFrame, double riskAversion, int latency, int cash, int asset ) {
        
        super( state, cash, asset );
        
        if ( timeFrame <= 0 ) {
            
            throw new MyException("Trader.constructor: timeFrame must be strictly positive");
        }
        
        if ( timeFrame < latency ) {
            
            throw new MyException("Trader.constructor: timeFrame < latency.");
        }
        
        this.timeFrame = timeFrame;
        this.riskAversion = riskAversion;
        this.latency = latency;
    }
    
    public abstract Event nextWakeUp( ccloop.MarketState state, RandNumGen rng );
    public abstract Event trade( ccloop.MarketState state, RandNumGen rng );
    
    @Override
    public String toString() {

	StringBuilder buf = new StringBuilder();

        buf.append(this.getClass());
        buf.append("(#").append(id);
        buf.append(",freq.= ").append(timeFrame);
        buf.append(",risk.av.= ").append(riskAversion);
        buf.append(",lat.= ").append(latency).append(")");

        return(buf.toString());
    }
}
