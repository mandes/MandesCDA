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
package abm.CuiBrabazon;

import abmlob.agents.Trader;
import abmlob.events.*;
import abmlob.orderbook.Order;
import abmlob.orderbook.WorkingQuote;
import ccloop.*;

public class CuiTrader extends Trader {

    // probNothing = 0.9847, probMarketOrder = 0.0003, probSubmitLimit = 0.0077, probCancelLimit = 0.0073
    // probCrossing = 0.0032, probInSpread = 0.0978, probSpread = 0.1726, probOffSpread = 0.7264
    
    // Order Size Type - Parameters of Log-normal Distribution
    // market order size - \mu = 7.5663, \sigma = 1.3355
    // crossing limit order size - \mu = 8.4701, \sigma = 1.1982
    // inside-spread limit order size - \mu = 7.8709,\sigma = 0.9799
    // spread limit order size -\mu = 7.8929, \sigma = 0.8571
    // off-spread limit order size -\mu = 8.2166,\sigma = 0.9545
    
    // Limit Price Type - Parameters of Power-law Distribution
    // off-spread relative limit price - xmin = 0.05, \beta = 1.7248

    public boolean buyTrader;

    public CuiTrader( MarketState state, int timeFrame, int latency, int cash, int assets, boolean buyTrader ) {
        
        super( state, timeFrame, 0, latency, cash, assets );
        
        this.type = 1;
        
        this.buyTrader = buyTrader;
    }
    
    @Override
    public void dispatch( Event evt, MarketState state ) {

        if ( evt.getClass() == EvtRandomPolling.class ) {

            Event tradeEvent = this.trade( state, state.rng );
            
            if ( tradeEvent != null ) {
            
                state.eventQueue.queue.add( tradeEvent );
            }
        }
    }
    
    @Override
    public Event nextWakeUp( MarketState state, RandNumGen rng ) {
        
        return null;
    }
    
    @Override
    public Event trade( MarketState state, RandNumGen rng ) {

    // probMarketOrder = 0.0196, probSubmitLimit = 0.5033, probCancelLimit = 0.4771

        if ( rng.nextDouble() <= 0.4771 ) { // prob of cancelation
            
            Order orderToBeRemoved = getOwnOldestOrder( buyTrader );

            if ( orderToBeRemoved != null ) {

                return new EvtRemoveOrder( state.eventQueue, state.clock.addTime(state.clock.getCurTime(), latency), orderToBeRemoved );
            }
            else {
                
                return null;
            }
        }
        else {  // submit market + limit order

            Order orderToBeSent = newRandOrder( buyTrader, (CuiMarketState)state, state.rng );

            if ( orderToBeSent != null ) {

                Event ev = new EvtSendNewOrder( state.eventQueue, state.clock.addTime(state.clock.getCurTime(), latency), orderToBeSent );
                return ev;
            }
            else {

                return null;
            }
        }
    }

    // order formation (expectation strategy) + order placement (microtrading)
    public Order newRandOrder( boolean isBuy, CuiMarketState state, RandNumGen rng ) {
        
        int size, limPrice = Consts.NULLPRICE;  // default (market orders)
        
        // log-normal distributions: exp(\mu +\sigma * rnorm)

        // probMarketOrder = 0.0375, probSubmitLimit = 0.9625
        boolean isMarket = ( rng.nextDouble() < 0.0375 ) ? true : false;
        
        if ( isMarket ) {
            
            // market order size - \mu = 7.5663,\sigma = 1.3355
            size = (int) Math.floor( rng.nextLogNormal( 7.5663, 1.3355 ) );
            
        }
        else {  // limit order

            WorkingQuote bidask = state.orderBook.getBidAskSpread( null );
            
            if ( bidask.bestAsk == 0 || bidask.bestBid == 0 ) {

                throw new MyException("CuiTrader.newRandOrder - order book should be filled");
            }

            double probAction = rng.nextDouble();
            
            // probCrossing = 0.0032, probInSpread = 0.0978, probSpread = 0.1726, probOffSpread = 0.7264
            
            if ( probAction <= 0.0032 ) { // crossing
                
                if ( isBuy ) {
                    
                    limPrice = bidask.bestAsk;
                }
                else {  // sell
                    
                    limPrice = bidask.bestBid;
                }
                
                // crossing limit order size - \mu = 8.4701,\sigma = 1.1982
                size = (int) Math.floor( rng.nextLogNormal( 8.4701, 1.1982 ) );
                
                state.agCrossLimOrdCnt++;
            }
            else {
                
                if ( probAction <= 0.1010 ) { // inside-spread = 0.0032 + 0.0978

                    limPrice = bidask.bestBid + 
                            (int) Math.floor( rng.nextDouble() * ( bidask.bestAsk - bidask.bestBid ) );

                    // inside-spread limit order size - \mu = 7.8709,\sigma = 0.9799
                    size = (int) Math.floor( rng.nextLogNormal( 7.8709, 0.9799 ) );
                    
                    state.agInSprLimOrdCnt++;
                }
                else {

                    if ( probAction <= 0.2736 ) { // spread = 0.1010 + 0.1726
                    
                        if ( isBuy ) {

                            limPrice = bidask.bestBid;
                        }
                        else {  // sell

                            limPrice = bidask.bestAsk;
                        }

                        // spread limit order size -\mu = 7.8929,\sigma = 0.8571
                        size = (int) Math.floor( rng.nextLogNormal( 7.8929, 0.8571 ) );
                        
                        state.agSprLimOrdCnt++;
                    }
                    else {  // off-spread = 0.2736 + 0.7264 = 1.0000

                        // off-spread relative limit price - xmin = 0.05, \beta = 1.7248
                        // xmin acts as an upper bound (truncated at the upper end)
                        int delta = (int) Math.round( rng.nextCuiPowerLaw( 1.7248, 0.05 ) * Math.pow( 10, Consts.PRICEDIGITS ) );

                        if ( delta < 0 ) {
                            
                            throw new MyException("CuiTrader.newRandOrder - relative price should be positive");
                        }
                        
                        if ( isBuy ) {

                            limPrice = bidask.bestBid - delta;
                            
                            //limPrice = (limPrice <= 0 ) ? 1 : limPrice;
                        }
                        else {  // sell

                            limPrice = bidask.bestAsk + delta;
                        }

                        if (limPrice <= 0 ) {
                            
                            throw new MyException("CuiTrader.newRandOrder - limitPrice must be strictly positive");
                        }
                                
                        // off-spread limit order size -\mu = 8.2166,\sigma = 0.9545
                        size = (int) Math.floor( rng.nextLogNormal(8.2166, 0.9545) );
                        
                        state.agOffSprLimOrdCnt++;
                    }
                }
            }
        }

        return new Order( state.orderBook, this, isBuy, !isMarket, size, limPrice, null );
    }


    public Order getOwnOldestOrder( boolean isBuy ) {
        
        if ( isBuy ) {
            
            if ( !portfolio.buyOrders.isEmpty() ) {

                return portfolio.buyOrders.last();
            }
        }
        else { // sell
            
            if ( !portfolio.sellOrders.isEmpty() ) {

                return portfolio.sellOrders.last();
            }
        }

        return null;
    }
}
