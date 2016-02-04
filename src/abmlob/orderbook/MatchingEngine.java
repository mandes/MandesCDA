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

import abmlob.events.*;
import ccloop.*;

public class MatchingEngine {    // manages order flow
    
    protected MarketState state;
    public OrderBook orderBook;
    public boolean IOC = false;    // imediate or cancel -- unexecuted part of a market order is canceled
    
    public MatchingEngine( MarketState state, OrderBook orderBook ) {
        
        this.state = state;
        this.orderBook = orderBook;
    }

    public void dispatch( Event evt ) {

        TimeStamp operTime = state.clock.getCurTime();
        
        WorkingQuote currentQuote = orderBook.getWorkingQuote( null );
        
        if ( evt.getClass() == EvtSendNewOrder.class ) {

            if ( validateOrder( ((EvtSendNewOrder)evt).order, operTime ) ) {

                placeNewOrder( ((EvtSendNewOrder)evt).order, operTime, evt );   // returns tracePrice
            }
            // else do nothing
        }
        
        if ( evt.getClass() == EvtModifyOrder.class ) {
            
            modifyOrder( ((EvtModifyOrder)evt).order, operTime,
                    ((EvtModifyOrder)evt).newLimitPrice,
                    ((EvtModifyOrder)evt).newOutstanding, evt );    // returns tracePrice
        }
        
        if ( evt.getClass() == EvtRemoveOrder.class ) {
        
            removeOrder( ((EvtRemoveOrder)evt).order, 5, operTime, evt );    // cancel
        }

        if ( evt.getClass() == EvtOrderExpiration.class ) {
        
            removeOrder( ((EvtOrderExpiration)evt).order, 6, operTime, evt );    // expiration
        }
        
        WorkingQuote updatedQuote = orderBook.getWorkingQuote( operTime );
        
        if ( !updatedQuote.equals( currentQuote ) ) {

            // best bid/ask change: market order, limit order (crossing, at- or inside-spread), cancel or expire limit order
            
            Quote q = new Quote( orderBook, updatedQuote );

            if ( !state.eventQueue.queue.add( new EvtNotifyQuoteChange( state.eventQueue, operTime, q ) ) ) { // priority 8
                
                throw new MyException("MatchingEngine.dispatch: could not add EvtNotifyQuoteChange to state.eventQueue.queue");
            }

        }
    }

    public boolean validateOrder( Order o, TimeStamp operTime ) {

        if ( o.outstanding < 1 ) {
            
            throw new MyException("MatchingEngine.validateOrder: size must be strictly positive");
        }

        if ( o.isLimit && o.limitPrice <= 0 ) {
            
            throw new MyException("MatchingEngine.validateOrder: limitPrice must be strictly positive");
        }

        if ( !o.isLimit ) {   // market orders
            
            if ( o.isBuy && orderBook.ask.isEmpty()) {
            
                return false;   // do nothing
            }
            
            if ( !o.isBuy && orderBook.bid.isEmpty()) {
            
                return false;
            }
        }
        
        if ( o.expirationTime != null && operTime.compareTo(o.expirationTime) >= 0 ) { // after

            throw new MyException("MatchingEngine.validateOrder: invalid expiration date (already expired)");
        }

        return true;
    }
    
    public int placeNewOrder( Order o, TimeStamp operTime, Event procEvent ) {
        
        //----- set time stamps & persist new order
        
        o.orderTime = operTime;
        o.priorityTime = operTime;
        o.lastUpdateTime = operTime;

        //----- update portfolio ( and order list )
        
        if ( o.isBuy ) {  // buy -> block cash
            
            if ( o.isLimit ) {  // buy limit

                int orderValue = Consts.priceToMoney( o.outstanding * o.limitPrice ); // cut-off digits
                
                o.agent.portfolio.cash -= orderValue;
                o.agent.portfolio.blockedCash += orderValue;
            }
            
            // in case of a BUY MARKET order, there is no portfolio pre-update
            // in case of non-IOC, portfolio is updated after order match: remaining order size at last trade price
            
            if ( !o.agent.portfolio.buyOrders.add(o) ) {
                
                throw new MyException("MatchingEngine.placeNewOrder: could not add order to agent.portfolio.buyOrders");
            }
        }
        else {  // sell -> block assets
            
            o.agent.portfolio.inventory -= o.outstanding;
            o.agent.portfolio.blockedInventory += o.outstanding;
            
            if ( !o.agent.portfolio.sellOrders.add(o) ) {
                
                throw new MyException("MatchingEngine.placeNewOrder: could not add order to agent.portfolio.sellOrders");
            }
        }
        
        //----- match order
        
        int tracePrice = matchOrder( o, operTime, procEvent );
        
        return tracePrice;
    }

    public int modifyOrder( Order o, TimeStamp operTime, int newLimitPrice, int newOutstanding, Event procEvent ) {
            
        if ( newOutstanding < 1 ) {
            
            throw new MyException("MatchingEngine.modifyOrder: newOutstanding must be strictly positive");
        }

        if ( newLimitPrice <= 0 ) {
            
            throw new MyException("MatchingEngine.modifyOrder: newLimitPrice must be strictly positive");
        }

        //----- remove old order
        
        if ( o.outstanding < 1 ) {
            
            return 0;   // no trade; the order to be modified was already filled in the meantime
        }

        // in order to update a TreeSet (comparator fields), first we have to remove the changed element and then update it

        if ( o.isBuy ) {
        
            if ( !o.agent.portfolio.buyOrders.remove(o) ) {
                
                throw new MyException("MatchingEngine.modifyOrder: could not remove changed order from agent buyList");
            }
            
            if ( !orderBook.bid.remove(o) ) {
                
                throw new MyException("MatchingEngine.modifyOrder: could not remove changed order from orderbook bid");
            }

        }
        else {  // sell
            
            if ( !o.agent.portfolio.sellOrders.remove(o) ) {
                
                throw new MyException("MatchingEngine.modifyOrder: could not remove changed order from agent sellList");
            }
            
            if ( !orderBook.ask.remove(o) ) {
                
                throw new MyException("MatchingEngine.modifyOrder: could not remove changed order from orderbook ask");
            }
        }
        
        // remove also related events ? cancel, expiration ? and then reinsert ( only expiration ) in match order

        state.eventQueue.removeEventAssociatedWith( o, procEvent );
        
        //----- update time stamps
        
        //o.orderTime = operTime;
        o.lastUpdateTime = operTime;

        if ( !( newLimitPrice == o.limitPrice && newOutstanding <= o.outstanding ) ) {
            
            o.priorityTime = operTime;  // loose priority
        }
        
        //----- update portfolio
        
        if ( o.isLimit && o.isBuy ) {  // buy limit
            
            int changeValue = Consts.priceToMoney( ( newOutstanding * newLimitPrice - o.outstanding * o.limitPrice ) ); // cut-off digits
            
            o.agent.portfolio.cash -= changeValue;
            o.agent.portfolio.blockedCash += changeValue;
        }

        if ( !o.isBuy ) {  // sell
            
            int changeSize = newOutstanding - o.outstanding;
            
            o.agent.portfolio.inventory -= changeSize;
            o.agent.portfolio.blockedInventory += changeSize;
        }
        
        //----- update order and order list
        
        o.outstanding = newOutstanding;
        o.limitPrice = newLimitPrice;

        if ( o.isBuy ) {
        
            if ( !o.agent.portfolio.buyOrders.add(o) ) {
                
                throw new MyException("MatchingEngine.modifyOrder: could not add order to agent.portfolio.buyOrders");
            }
        }
        else {
            
            if ( !o.agent.portfolio.sellOrders.add(o) ) {
                
                throw new MyException("MatchingEngine.modifyOrder: could not add order to agent.portfolio.sellOrders");
            }
        }
        
        //----- match order
        
        int tracePrice = matchOrder(o, operTime, procEvent);
        
        return tracePrice;
    }
    
    private int matchOrder( Order o, TimeStamp operTime, Event procEvent ) {   // clearing mechanism

        int tradeSize, tradePrice = 0;
        Order cp = null;   // trade counter-party

        //-------------------------------- buy order --------------------------------
        
        if ( o.isBuy ) {
            
            //----- if market order or tradeable limit order

            while ( o.outstanding > 0 && !orderBook.ask.isEmpty() && 
                    ( !o.isLimit || ( orderBook.ask.first().limitPrice <= o.limitPrice ) ) ) 
            {
                
                //----- trade with counterparty

                cp = orderBook.ask.first();  // best ask
                
                tradePrice = cp.limitPrice;
                tradeSize = ( o.outstanding < cp.outstanding ) ? o.outstanding : cp.outstanding;
                
                Trade t = new Trade( orderBook, tradePrice, tradeSize, operTime, o, cp, true );

                EvtNotifyTrade newTradeEv = new EvtNotifyTrade( state.eventQueue, operTime, t );
                        
                if ( !state.eventQueue.queue.add( newTradeEv ) ) {   // priority 8
                    
                    throw new MyException("MatchingEngine.matchOrder.buy: could not add EvtNotifyTrade to state.eventQueue.queue");
                }
                
                if ( Consts.DEBUGMODE ) { System.out.println(t); }
                
                //----- update (passive) seller portfolio and sell order

                cp.agent.portfolio.blockedInventory -= tradeSize;
                cp.agent.portfolio.cash += Consts.priceToMoney( tradeSize * tradePrice );  // cut-off digits

                cp.outstanding -= tradeSize;
                cp.lastUpdateTime = operTime;

                if ( cp.outstanding == 0 ) {

                    //----- persist full fill (2)
                    
                    //----- remove from agent's list
                    
                    if ( !cp.agent.portfolio.sellOrders.remove(cp) ){
                        
                        throw new MyException("MatchingEngine.matchOrder.buy: could not remove filled cp #"+ cp.id +" from agent.portfolio.sellOrders");
                    }
                    
                    //----- remove from orderbook
                    
                    if( orderBook.ask.pollFirst() == null ) {
                        
                        throw new MyException("MatchingEngine.matchOrder.buy: could not remove filled cp #"+ cp.id +" from orderBook.ask");
                    }
                    
                    //----- remove also any future related events (remove, etc.)
                    
                    state.eventQueue.removeEventAssociatedWith(cp, procEvent);

                }
                else {
                    
                    //----- persist partial fill (3)
                }
                
                //----- update buy order and buyer portfolio

                o.outstanding -= tradeSize;
                o.agent.portfolio.inventory += tradeSize;

                if ( o.isLimit ) {
                    
                    o.agent.portfolio.blockedCash -= Consts.priceToMoney( tradeSize * o.limitPrice );
                    
                    // trade price might be smaller (better) than order limit price -> unblock more cash
                    o.agent.portfolio.cash += Consts.priceToMoney( tradeSize * ( o.limitPrice - tradePrice ) );

                }
                else {  // buy market
                    
                    o.agent.portfolio.cash -= Consts.priceToMoney( tradeSize * tradePrice );
                }
                
                if ( o.outstanding == 0 ) {
                    
                    //----- persist full fill (2)

                    if ( !o.agent.portfolio.buyOrders.remove(o) ) {
                        
                        throw new MyException("MatchingEngine.matchOrder.buy: could not reomve filled order");
                    }
                }
                else {
                    
                    //----- persist partial fill (3)
                }
                
            } // end while loop
            
            //----- matching finished
            
            if ( o.outstanding > 0 ) {  // not fully matched

                //----- market order
                
                if ( !o.isLimit ) {
                    
                    if ( IOC ) {
                        
                        //----- cancel outstanding
                        
                        if ( !o.agent.portfolio.buyOrders.remove(o) ) {

                            throw new MyException("MatchingEngine.matchOrder.buy - outstanding not removed");
                        }

                        return tradePrice;
                        
                    }
                    else {  // non-IOC
                        
                        if ( cp == null ) {   // not traded

                            // should not arrive here
                            throw new MyException("MatchingEngine.matchOrder.buy: buy market order against empty book");
                        }

                        //----- transform into a limit order
                     
                        o.isLimit = true;
                        o.limitPrice = tradePrice;

                        //----- adjust portfolio with remaining size at computed limit price

                        int orderValue = Consts.priceToMoney( o.outstanding * o.limitPrice );

                        o.agent.portfolio.cash -= orderValue;
                        o.agent.portfolio.blockedCash += orderValue;

                    }
                }
                
                //------ limit order ( initial or converted from market order in the non-IOC case )

                if ( o.isLimit ) {
                    
                    //----- add to orderbook
                    
                    if ( !orderBook.bid.add(o) ) {
                        
                        throw new MyException("MatchingEngine.matchOrder.buy: could not add order to orderBook.bid");
                    }

                    //----- add expiry event
                    
                    if ( o.expirationTime != null ) {

                        EvtOrderExpiration orderExpEv = new EvtOrderExpiration( state.eventQueue, o.expirationTime, o );
                                
                        if ( !state.eventQueue.queue.add( orderExpEv ) ) {
                            
                            throw new MyException("MatchingEngine.matchOrder.buy: could not add EvtOrderExpiration to state.eventQueue.queue");
                        }
                    }
                }
            }
            
            return tradePrice;
        }
        
        //-------------------------------- sell order --------------------------------
        
        else {

            while ( o.outstanding > 0 && !orderBook.bid.isEmpty() && 
                    ( !o.isLimit || ( orderBook.bid.first().limitPrice >= o.limitPrice ) ) ) 
            {
                
                //----- trade with counterparty
                
                cp = orderBook.bid.first();
                
                tradePrice = cp.limitPrice;
                tradeSize = ( o.outstanding < cp.outstanding ) ? o.outstanding : cp.outstanding;
                
                Trade t = new Trade( orderBook, tradePrice, tradeSize, operTime, cp, o, false );

                EvtNotifyTrade newTradeEv = new EvtNotifyTrade( state.eventQueue, operTime, t );
                        
                if ( !state.eventQueue.queue.add( newTradeEv ) ) {   // priority 8
                    
                    throw new MyException("MatchingEngine.matchOrder.sell: could not add EvtNotifyTrade to state.eventQueue.queue");
                }
                
                if ( Consts.DEBUGMODE ) { System.out.println(t); }

                //----- update sell order and seller portfolio
                
                o.outstanding -= tradeSize;
                
                o.agent.portfolio.blockedInventory -= tradeSize;
                o.agent.portfolio.cash += Consts.priceToMoney( tradeSize * tradePrice );

                if ( o.outstanding == 0 ) {
                    
                    //----- persist full fill
                    
                    if ( !o.agent.portfolio.sellOrders.remove(o) ) {
                        
                        throw new MyException("MatchingEngine.matchOrder.sell: could not remove filled order from agent.portfolio.sellOrders");
                    }
                    
                }
                else {
                    
                    //----- persist partial fill
                }

                //-------- update (passive) buyer portfolio and buy order
                
                cp.agent.portfolio.inventory += tradeSize;
                cp.agent.portfolio.blockedCash -= Consts.priceToMoney( tradeSize * tradePrice );

                cp.outstanding -= tradeSize;
                cp.lastUpdateTime = operTime;

                if ( cp.outstanding == 0 ) {

                    //----- persist full fill
                    
                    //----- remove from agent's list
                    
                    if ( !cp.agent.portfolio.buyOrders.remove(cp) ) {
                     
                        throw new MyException("MatchingEngine.matchOrder.sell: could not remove filled cp from agent.portfolio.buyOrders");
                    }
                    
                    //----- remove from orderbook
                    
                    if( orderBook.bid.pollFirst() == null ) {
                        
                        throw new MyException("MatchingEngine.matchOrder.sell: could not remove filled cp from orderBook.bid");
                    }
                    
                    //----- remove also any future related events (remove, etc.)
                    
                    state.eventQueue.removeEventAssociatedWith(cp, procEvent);

                }
                else {
                    
                    //----- persist partial fill
                }

            } // end while
            
            //----- matching finished
            
            if ( o.outstanding > 0 ) {  // not fully matched

                //----- market order
                
                if ( !o.isLimit ) {
                    
                    if ( IOC ) {
                        
                        //----- cancel outstanding
                        
                        if ( !o.agent.portfolio.sellOrders.remove(o) ) {

                            throw new MyException("MatchingEngine.matchOrder.sell: could not remove outstanding order from agent.portfolio.sellOrders");
                        }
                        
                        //----- undo portfolio position
                        
                        o.agent.portfolio.blockedInventory -= o.outstanding;
                        o.agent.portfolio.inventory += o.outstanding;
                        
                        //----- persist order and portfolio changes

                        return tradePrice;
                        
                    }
                    else { // non-IOC

                        if ( cp == null ) {   // not traded

                            // should not arrive here
                            throw new MyException("MatchingEngine.matchOrder.sell: sell market order against empty book");
                        }

                        //----- transform into limit order (portfolio was already adjusted)

                        o.isLimit = true;   
                        o.limitPrice = tradePrice; // at the last trade price
                    }

                }
                
                //----- limit order (initial or converted)
                
                if ( o.isLimit ) {

                    //----- add to order book
                    
                    if ( !orderBook.ask.add(o) ) {
                        
                        throw new MyException("MatchingEngine.matchOrder.sell: could not add order to orderBook.ask");
                    }

                    //----- add expiry event

                    if ( o.expirationTime != null ) {

                        EvtOrderExpiration orderExpEv = new EvtOrderExpiration( state.eventQueue, o.expirationTime, o );
                        
                        if ( !state.eventQueue.queue.add( orderExpEv ) ) {
                            
                            throw new MyException("MatchingEngine.matchOrder.sell: could not add EvtOrderExpiration to state.eventQueue.queue");
                        }
                    }
                }
            }
            
            return tradePrice;
        }
    }

    // changeType: 5 - cancel, 6 - expiration
    public boolean removeOrder( Order o, int changeType, TimeStamp operTime, Event procEvent ) {
        
        o.lastUpdateTime = operTime;
        
        if ( o.isBuy ) {
            
            if ( !o.agent.portfolio.buyOrders.remove(o) ) {
                
                throw new MyException("MatchingEngine.removeOrder: could not remove order from agent.portfolio.buyOrders");
            }
            
            if ( !orderBook.bid.remove(o) ) {
                
                throw new MyException("MatchingEngine.removeOrder: could not remove order from orderBook.bid");
            }

            int orderValue = Consts.priceToMoney( o.outstanding * o.limitPrice );

            o.agent.portfolio.cash += orderValue;
            o.agent.portfolio.blockedCash -= orderValue;
            
        }
        else {  // sell
            
            if ( !o.agent.portfolio.sellOrders.remove(o) ) {
                
                throw new MyException("MatchingEngine.removeOrder: O#"+ o.id + " not removed from agent.portfolio.sellOrders");
            }
            
            if ( !orderBook.ask.remove(o) ) {
                
                throw new MyException("MatchingEngine.removeOrder: could not remove order from orderBook.ask");
            }
            
            o.agent.portfolio.inventory += o.outstanding;
            o.agent.portfolio.blockedInventory -= o.outstanding;
        }

        //----- persist changes

        //----- remove also any future related events (expiration, cancel with latency)
        
        state.eventQueue.removeEventAssociatedWith(o, procEvent);
        
        return true;
    }
}
