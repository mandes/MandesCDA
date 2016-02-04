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
package abmlob.orderbook;

import ccloop.Consts;
import ccloop.TimeStamp;

public class Quote extends OrderBookArtifact implements QuoteInterface {
 
    public TimeStamp time;
    
    public int bestBid;
    public int bestBidVol;
    
    public int bestAsk;
    public int bestAskVol;
    
    public Quote ( OrderBook ob, TimeStamp time, int bestBid, int bestBidVol, int bestAsk, int bestAskVol ) {
        
        super( ob );    // quote id
        
        this.time = time;
        this.bestBid = bestBid;
        this.bestBidVol = bestBidVol;
        this.bestAsk = bestAsk;
        this.bestAskVol = bestAskVol;
    }

    public Quote ( OrderBook ob, WorkingQuote wq ) {
        
        super( ob );    // quote id
        
        this.time = wq.getTime();
        this.bestBid = wq.getBestBid();
        this.bestBidVol = wq.getBestBidVol();
        this.bestAsk = wq.getBestAsk();
        this.bestAskVol = wq.getBestAskVol();
    }

    @Override
    public int getBestBid() { return bestBid; }
    
    @Override
    public int getBestAsk() { return bestAsk; }
    
    @Override
    public int getBestBidVol() { return bestBidVol; }
    
    @Override
    public int getBestAskVol() { return bestAskVol; }
    
    @Override
    public TimeStamp getTime() { return time; }

    @Override 
    public boolean equals(final Object obj) {

        if ( obj == null || !( obj instanceof QuoteInterface ) ) {
            
           return false;
        }
        
        return ( this.bestBid == ((QuoteInterface)obj).getBestBid() &&
                 this.bestBidVol == ((QuoteInterface)obj).getBestBidVol() &&
                 this.bestAsk == ((QuoteInterface)obj).getBestAsk() &&
                 this.bestAskVol == ((QuoteInterface)obj).getBestAskVol() );
    }

    @Override
    public int hashCode() {

        int hash = 7;

        hash = 59 * hash + this.bestBid;
        hash = 59 * hash + this.bestBidVol;
        hash = 59 * hash + this.bestAsk;
        hash = 59 * hash + this.bestAskVol;

        return hash;
    }

    @Override
    public String toString() { 

        StringBuffer buf = new StringBuffer();

        buf.append("Quote #").append(this.id).append(" (bid: ");
        buf.append(bestBidVol).append(" @").append( (double)this.bestBid / Math.pow(10,Consts.PRICEDIGITS) );
        buf.append(", ask: ");
        buf.append(bestAskVol).append(" @").append( (double)this.bestAsk / Math.pow(10,Consts.PRICEDIGITS) );
        buf.append(")");

        return(buf.toString());
    }
}
