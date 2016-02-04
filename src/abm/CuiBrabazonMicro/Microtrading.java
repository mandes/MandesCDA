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
package abm.CuiBrabazonMicro;

import abmlob.agents.Agent;
import abmlob.orderbook.Order;
import abmlob.orderbook.WorkingQuote;
import ccloop.*;
import java.util.Iterator;

// order placement
// in: investment decision, out: market/ limit order

public class Microtrading {

    MarketState state;

    //--- market variables
    
    public int defaultADV = 77000000;
    public double emaPrice;
    public double instantVola;      // EMA of the squared returns

    //--- functional params
    
    public int obiLevels;
    public double obiBase;
    
    // the smaller the exponent, the bigger the penalty
    public double sizePenaltyExp; // nu

    //--- volatility multipliers
    
    public double isSigmaMult;
    public double dynSigmaMult;

    //--- scaling/ tuning params
    
    public double alpha0;
    public double alpha1;
    public double alpha2;
    
    public double beta;

    public Microtrading( MarketState state ) {
        
        this.state = state;
        
        this.emaPrice = Consts.NULLPRICE;
    }
    
    public Order articulateOrder ( Agent a, boolean isBuy, int size, Benchmarks isType, Benchmarks dynType,
            double urgencyCoeff, double stdDev, int avDailyVol, TimeStamp expiryTimeStamp ) {

        // IN: buy/sell, size, benchmarks, riskAv, stdDev, ADV
        // OUT: M, delta
            
        // alternatively, the percentage size could be relative to the average trade size
        double percSize = (double) size * 100 / avDailyVol;

        double isSigma = isSigmaMult * stdDev;
        double dynSigma = dynSigmaMult * stdDev;

        //------ get best quote
        
        WorkingQuote bestQuote = state.orderBook.getBidAskSpread( null );

        //------ set base & spread

        int base;

        if ( isBuy ) {
            
            if ( bestQuote.bestAsk != 0 ) {
                
                base = bestQuote.bestAsk;
            }
            else {
                
                // alternatively previous valid ask quote from state.quoteHistory
                
                if ( bestQuote.bestBid != 0 ) {
                    
                    base = bestQuote.bestBid;
                }
                else {
                    
                    base = !state.tradeHistory.isEmpty() ? state.tradeHistory.getLast().price : Consts.NULLPRICE;
                }
            }
        }
        else {
            
            if ( bestQuote.bestBid != 0 ) {
                
                base = bestQuote.bestBid;
            }
            else {
                
                if ( bestQuote.bestAsk != 0 ) {

                    base = bestQuote.bestAsk;
                }
                else {

                    base = !state.tradeHistory.isEmpty() ? state.tradeHistory.getLast().price : Consts.NULLPRICE;
                }
            }
        }

        double percSpread = ( bestQuote.bestBid != 0 && bestQuote.bestAsk != 0) ? 
                (double) ( bestQuote.bestAsk - bestQuote.bestBid ) * 100 / base : (double) 100 / base;

        //------ set relative benchmarks
        
        double isBm, dynBm;

        switch ( isType ) {     // for implmentation shortfall

            case PrevClosePrice:
                
                isBm = ( (double) Consts.NULLPRICE / base - 1 ) * 100;
                break;
            
            case LastTradePrice:
                
                int lastTradePrice = Consts.NULLPRICE;
                
                if ( !state.tradeHistory.isEmpty() ) {
                    
                    lastTradePrice = state.tradeHistory.getLast().price;
                }

                if ( isBuy ) {  // base = bestAsk
                
                    isBm = ( (double) base / lastTradePrice - 1 ) * 100;
                }
                else {  // base = bestBid
                    
                    isBm = ( (double) lastTradePrice / base - 1 ) * 100;
                }

                break;

            case EmaPrice:
                
                if ( isBuy ) {  // base = bestAsk
                
                    isBm = ( (double) base / emaPrice - 1 ) * 100;
                }
                else {  // base = bestBid
                    
                    isBm = ( (double) emaPrice / base - 1 ) * 100;
                }
                
                break;
            
            default:  // ArrivalPrice

                isBm = percSpread / 2;
        }
        
        switch ( dynType ) {    // for volatility-bands

            case BestOppositeQuote:
                  
                dynBm = 0.0;
                break;
                
            case LastTradePrice:
                  
            case EmaPrice:
                
            default:  // ArrivalPrice
                  
                dynBm = isBm;
        }
        
        //------ market order branch ( M = 1 ) evaluation

        double mkOrdFitness;    // f( M = 1 )
        
        double percMkImp = getMkImp( isBuy, size );

        if ( percMkImp < 0 ) {  // unable to fill the entire order
            
            mkOrdFitness = Double.MAX_VALUE;
        }
        else {

            double mkOrdImpSh = isBm + percMkImp;
            
            if ( mkOrdImpSh <= isSigma ) {
                
                mkOrdFitness = mkOrdImpSh;
            }
            else {
                
                mkOrdFitness = beta * Math.pow( mkOrdImpSh, 2) / isSigma;
            }
        }
        
        //------ limit order branch ( M = 0 ) evaluation

        double obi = getOrderBookImb( obiLevels, size, isBuy );
        
        double A = urgencyCoeff * Math.pow( obiBase, obi ) * compSizePenalty( percSize );

        // call numerical procedure for optimising delta
        double[] limDistAndFit = getOptimalLimDist( isBuy, base, isBm, isSigma, dynBm, dynSigma, avDailyVol, A, beta );        
        double percRelDist = limDistAndFit[0];   // \Delta^*
        double limOrdFitness = limDistAndFit[1];     // f( M = 0, \Delta^* )
        
        //------ order choice
        
        if ( mkOrdFitness <= limOrdFitness ) {
            
            return new Order( state.orderBook, a, isBuy, false, size, 0, null ); // market order
        }
        else {  // isLimit

            int limitPrice;
            
            if ( isBuy ) {
                
                limitPrice = (int) ( (double) base * ( 1 - percRelDist / 100 ) );
                
                limitPrice = ( limitPrice < 1 ) ? 1 : limitPrice;
            }
            else {
                
                limitPrice = (int) ( (double) base * ( 1 + percRelDist / 100 ) );
            }
            
            if ( limitPrice <= 0 ) {
            
                throw new MyException("Microtrading.articulateOrder - limitPrice must be strictly positive");
            }

            return new Order( state.orderBook, a, isBuy, true, size, limitPrice, expiryTimeStamp );
        }
    }
    
    public double[] getOptimalLimDist ( boolean isBuy, int base, double bm1, double sigma1, double bm2, double sigma2, int adv, double A, double beta ) {

        Iterator<Order> bookIterator;
        int inc;
        
        if ( isBuy ) {

            bookIterator = state.orderBook.bid.iterator();
            inc = 1;
        }
        else {

            bookIterator = state.orderBook.ask.iterator();
            inc = -1;
        }

        //------ start numerical procedure
       
        double delta = 1 * 100 / base;  // minimum price step in percentage

        double percQueue = 0.0; // not quite -- need to be checked
        
        double bestDelta = delta;   // initial delta
        double bestFit = compDeltaQueueFit( delta, percQueue, bm1, sigma1, bm2, sigma2, A, beta );
        
        double deltaMax = getStoppingPoint( bm1, sigma1, bm2, sigma2, A, beta );
        
        int curPriceLevel = 0, queueSize = 0;
        
        while ( bookIterator.hasNext() && delta < deltaMax ) {
            
            Order o = bookIterator.next();
            
            if ( o.limitPrice != curPriceLevel ) { // new level

                //----- try delta just in front of this level or deltaMax

                delta = (double) Math.abs( o.limitPrice - base + inc ) * 100 / base;

                delta = ( delta > deltaMax ) ? deltaMax : delta;    // cut-off

                percQueue = (double) queueSize * 100 / adv;
                
                double fit = compDeltaQueueFit( delta, percQueue, bm1, sigma1, bm2, sigma2, A, beta );

                if ( fit < bestFit ) {
                    
                    bestFit = fit;
                    bestDelta = delta;
                }

                curPriceLevel = o.limitPrice;
                queueSize += o.outstanding; 
            }
            else {
            
                queueSize += o.outstanding; 
            }
        }

        if ( delta < deltaMax ) {   // check also deltaMax
            
            double fit = compDeltaQueueFit( deltaMax, percQueue, bm1, sigma1, bm2, sigma2, A, beta );
         
            if ( fit < bestFit ) {

                bestFit = fit;
                bestDelta = deltaMax;
            }
        }

        return new double[]{bestDelta, bestFit};
    }
    
    public double getStoppingPoint ( double bm1, double sigma1, double bm2, double sigma2, double A, double beta ) {

        double deltaMax;
        double threshold = bm1 - sigma1;
        
        if ( threshold < 0 ) {
            
            deltaMax = bm2 + sigma2 / ( 2 * alpha1 * A);
        }
        else {
            
            double deltaMax1 = ( beta * bm1 * sigma2 + A * alpha1 * bm2 * sigma1 ) / ( beta * sigma2 + alpha1 * A * sigma1 );
            double deltaMax2 = bm2 + sigma2 / ( 2 * alpha1 * A);
            
            if ( deltaMax1 < threshold ) {
                
                deltaMax = deltaMax1;
            }
            else {
                
                if ( deltaMax2 >= threshold ) {
                
                    deltaMax = deltaMax2;
                }
                else {
                    
                    throw new MyException("Microtrading.getStoppingPoint - should not be here");
                }
            }
        }
        
        return deltaMax;
    }

    public double compDeltaQueueFit ( double delta, double percQueue, double bm1, double sigma1, double bm2, double sigma2, double A, double beta ) {
        
        double cost, adjRisk;
        double threshold = bm1 - sigma1;
        
        if ( delta >= threshold ) {

            cost = bm1 - delta;  // implementation shortfall
        }
        else {

            cost = beta * Math.pow( bm1 - delta, 2 ) / sigma1;
        }

        adjRisk = A * ( alpha0 + alpha1 * compVolaPenalty( delta, bm2, sigma2 ) + alpha2 * compQueuePenalty( percQueue ) );
        
        return ( cost + adjRisk );
    }
    
    public double getMkImp ( boolean isBuy, int outstanding ) {

        int bestPrice = 0, tracePrice = 0;

        Iterator<Order> bookIterator;
        
        if ( isBuy ) {
            
            bookIterator = state.orderBook.ask.iterator();
            bestPrice = state.orderBook.getBidAskSpread( null ).bestAsk;
        }
        else {
            
            bookIterator = state.orderBook.bid.iterator();
            bestPrice = state.orderBook.getBidAskSpread( null ).bestBid;
        }

        while ( outstanding > 0 && bookIterator.hasNext() ) {

            Order o = bookIterator.next();

            tracePrice = o.limitPrice;
            outstanding -= o.outstanding;
        }
        
        if ( outstanding >= 0 ) {   // the market order cannot be entirely filled or the order book is emptied
            
            return ( -1.0 );
        }
        else {
        
            double percImp = (double) Math.abs( bestPrice - tracePrice ) * 100 / bestPrice;
            return ( percImp );
        }
    }

    public double getOrderBookImb ( int depth, int size, boolean isBuy ) {

        // if bestBid == 0 and bestAsk == 0 then OBI = 0
        // if bestBid == 0 and bestAsk != 0 then OBI = 1 (buy), -1 (sell)
        // if bestBid != 0 and bestAsk == 0 then OBI = -1 (buy), 1 (sell)
        
        // else (bestBid != 0 and bestAsk != 0):

        int totalBid = 0, totalAsk = 0;        
        int levelCount = 0, levelPrice = 0;

        Iterator<Order> bidIterator = state.orderBook.bid.iterator();

        while ( bidIterator.hasNext() ) {

            Order b = bidIterator.next();
            
            if ( b.limitPrice != levelPrice ) {
                
                levelCount++;
                if ( levelCount > depth ) {

                    break;
                }
                
                levelPrice = b.limitPrice;
            }
            
            totalBid += b.outstanding;
        }

        Iterator<Order> askIterator = state.orderBook.ask.iterator();
        levelCount = levelPrice = 0;
        
        while ( askIterator.hasNext() ) {

            Order a = askIterator.next();
            
            if ( a.limitPrice != levelPrice ) {
                
                levelCount++;
                if ( levelCount > depth ) {

                    break;
                }
                
                levelPrice = a.limitPrice;
            }
            
            totalAsk += a.outstanding;
        }
/*
        if ( isBuy ) {

            totalBid += size;
        }
        else {

            totalAsk += size;
        }
*/
        return (double) ( totalBid - totalAsk ) * (isBuy ? 1 : -1) / ( totalBid > totalAsk ? totalBid : totalAsk );
    }

    public double compSizePenalty ( double percSize ) {

        return Math.exp( Math.pow( percSize, sizePenaltyExp ) );
    }

    public double compQueuePenalty ( double percQueue ) {

        return percQueue;
    }

    public double compVolaPenalty ( double delta, double bm2, double sigma2 ) {

        return Math.pow( delta - bm2, 2 ) / sigma2;
    }

}
