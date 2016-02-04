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

import abmlob.agents.*;
import ccloop.*;

public class Order extends OrderBookArtifact {

    public Agent agent;
    public TimeStamp orderTime; // initial order time
    public TimeStamp lastUpdateTime;    // not used in sorting
    public TimeStamp priorityTime;

    public boolean isBuy;
    public boolean isLimit;
    
    public int outstanding;
    public int limitPrice;
    public TimeStamp expirationTime;
    
    public Order( OrderBook ob, Agent agent, boolean isBuy, boolean isLimit, int size, int price, TimeStamp expirationTime ) {

        super( ob );    // order id

        this.agent = agent;
        
        this.isBuy = isBuy;
        this.isLimit = isLimit;
        this.outstanding = size;
        this.limitPrice = price;
        this.expirationTime = expirationTime;
    }
    
    public Order ( Order source ) {
        
        super();
        
        this.id = source.id;
        this.agent = null;      // !!!!!!!
        
        this.orderTime = source.orderTime;
        this.lastUpdateTime = source.lastUpdateTime;
        this.priorityTime = source.priorityTime;
        
        this.isBuy = source.isBuy;
        this.isLimit = source.isLimit;
        
        this.outstanding = source.outstanding;
        this.limitPrice = source.limitPrice;
        this.expirationTime = source.expirationTime;
    }

    // not necessary for TreeSet
    @Override 
    public boolean equals( final Object obj ) {
        
        if (obj == null || this.getClass() != obj.getClass()) {
            
           return false;
        }
        
        return this.id == ((Order)obj).id;
    }

    @Override
    public int hashCode() {
        
        int hash = 7;
        hash = 13 * hash + (int) (this.id ^ (this.id >>> 32));
        
        return hash;
    }
 
    @Override
    public String toString() { 
        
        StringBuffer buf = new StringBuffer();

        buf.append("Order(#").append( id );
        buf.append(",A#").append( agent.id );
        buf.append(" ").append( isBuy ? "buy" : "sell" );
        buf.append(" ").append( isLimit ? "limit" : "market" );
        buf.append(" ").append( outstanding );

        if (isLimit) {

            buf.append(" @").append( (double)limitPrice / Math.pow(10,Consts.PRICEDIGITS) );
        }

        buf.append(")");

        return(buf.toString());
    }
}
