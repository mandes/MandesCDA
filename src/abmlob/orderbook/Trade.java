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

import abmlob.agents.Agent;
import ccloop.Consts;
import ccloop.TimeStamp;

public class Trade extends OrderBookArtifact {
    public TimeStamp time;

    public Agent buyAgent;
    public Agent sellAgent;

    public int size;
    public int price;

    public boolean buyerInit;

    // convention: o1 buys, o2 sells

    public Trade( OrderBook ob, int price, int size, TimeStamp time, Order buyOrder, Order sellOrder, boolean buyerInit ) { 

        super( ob );    // trade id
        this.time = time;

        this.buyAgent = buyOrder.agent;
        this.sellAgent = sellOrder.agent;

        this.price = price;
        this.size = size;
        this.buyerInit = buyerInit;
    }
    
    @Override
    public String toString() { 
        
        StringBuffer buf = new StringBuffer();

        buf.append("Trade (T#").append(id).append(", ");
        buf.append(time).append(", ");
        buf.append(size).append(" @").append( (double)this.price / Math.pow(10,Consts.PRICEDIGITS) );
        buf.append(")");

        return(buf.toString());
    }
}
