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

import abmlob.orderbook.*;
import ccloop.Consts;
import java.util.TreeSet;

public class Portfolio {
    
    public int cash;
    public int blockedCash;
    
    public int inventory;
    public int blockedInventory;

    public TreeSet<Order> buyOrders;
    public TreeSet<Order> sellOrders;
    
    public Portfolio (int cash, int inventory) {
        
        this.cash = cash;
        this.blockedCash = 0;
        
        this.inventory = inventory;
        this.blockedInventory = 0;

        OrderCompByOrderTime comp = new OrderCompByOrderTime();
        
        this.buyOrders = new TreeSet<>(comp);
        this.sellOrders = new TreeSet<>(comp);
    }
    
    @Override
    public String toString() {

	StringBuilder buf = new StringBuilder();

        buf.append("Portfolio($").append( (double)cash / Math.pow(10,Consts.CASHDIGITS) );
        buf.append("/ $").append( (double)blockedCash / Math.pow(10,Consts.CASHDIGITS) ).append(" blk. in ").append(buyOrders.size()).append(" ord.");
        buf.append(", Inv ").append(inventory);
        buf.append("/ ").append(blockedInventory).append(" blk. in ").append(sellOrders.size()).append(" ord.");
        buf.append(")\n");

        return(buf.toString());
    }
}
