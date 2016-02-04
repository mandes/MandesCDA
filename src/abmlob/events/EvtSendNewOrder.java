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
package abmlob.events;

import abmlob.orderbook.Order;
import ccloop.TimeStamp;

public class EvtSendNewOrder extends Event {

    public Order order;
    
    public EvtSendNewOrder( EventQueue queue, TimeStamp eventTime, Order orderToBeSent ) {

        super( queue );
        
        this.eventTime = eventTime;
        this.order = orderToBeSent;
    }
    
    public EvtSendNewOrder( EventQueue queue, TimeStamp eventTime, Order orderToBeSent, int priority ) {

        super( queue );
        
        this.eventTime = eventTime;
        this.order = orderToBeSent;
        this.priority = priority;
    }
    
    @Override
    public String toString() { 

        StringBuffer buf = new StringBuffer();
        
        buf.append(eventTime).append("-#").append( order.agent.id ).append("-").append(getType());
        
        return(buf.toString());
    }

}
