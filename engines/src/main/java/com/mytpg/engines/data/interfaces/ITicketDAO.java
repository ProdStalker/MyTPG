package com.mytpg.engines.data.interfaces;

import com.mytpg.engines.entities.Ticket;

import java.util.List;

/**
 * Created by stalker-mac on 20.11.14.
 */
public interface ITicketDAO {
    long countActive();
    List<Ticket> getAll(boolean ArgIsFull);
}
