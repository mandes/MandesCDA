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

import ccloop.MarketState;
import ccloop.MyException;
import java.util.HashMap;
import java.util.Map;

public class EventDispatcher {
    
    public Map<Class<? extends Event>, Handler> handlers;
    
    public EventDispatcher() {
        
        handlers = new HashMap<>();
    }

    public void registerChannel(Class<? extends Event> contentType, Handler channel) {
        
        handlers.put(contentType, channel);
    }

    // the handle which is linked to the type of event is activated

    public void dispatch(Event content, MarketState state) {
        
        Handler h = handlers.get(content.getClass());
        
        if ( h == null ) {
        
            throw new MyException("Missing Handler for the queued Event.");
        }
        
        state.clock.updateTime(content.eventTime);
        
        h.broadcast(content, state);
    }
}
