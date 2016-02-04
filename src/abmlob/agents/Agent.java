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

import abmlob.events.*;
import ccloop.MarketState;
import ccloop.MyException;

public abstract class Agent {    // an agent is a message handler
    
    public int id;
    
    public int type = 0; // default
    public Portfolio portfolio;

    public Agent( MarketState state, int cash, int asset ) {
        
        if ( state.instancedAgents == Integer.MAX_VALUE ) {
            
            throw new MyException("Agent: id numeric overflow");
        }
        
        state.instancedAgents++;
        id = state.instancedAgents;   // start with 1
        
        this.portfolio = new Portfolio(cash, asset);
    }
    
    // activation - demultiplexing events - process message
    public void dispatch( Event message, MarketState state ) {

        System.out.println("Agent #" + id + " is active on " + message.getType());
    }

    @Override
    public String toString() {

	StringBuilder buf = new StringBuilder();

        buf.append(this.getClass());
        buf.append("(#").append(id).append(")");

        return( buf.toString() );
    }
}
