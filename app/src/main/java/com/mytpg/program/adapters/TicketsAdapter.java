package com.mytpg.program.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.mytpg.engines.entities.Ticket;
import com.mytpg.engines.entities.Tutorial;
import com.mytpg.engines.tools.DateTools;
import com.mytpg.program.R;
import com.mytpg.program.adapters.core.CoreAdapter;
import com.mytpg.program.core.App;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by stalker-mac on 16.08.16.
 */
public class TicketsAdapter extends CoreAdapter<TicketsAdapter.ViewHolder> {
    private Context mContext;
    private AdapterView.OnItemClickListener mItemClickListener = null;
    private List<Ticket> mTickets;


    public ArrayList<Ticket> getTicketsToSave() {
        ArrayList<Ticket> tickets = new ArrayList<>();

        if (getItemCount() > 0)
        {
            for (Ticket ticket : mTickets)
            {
                tickets.add(ticket);
            }
        }

        return tickets;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public View m_baseView = null;
        public TextView mNameTV = null;
        public TextView mDescriptionTV = null;
        public TextView mPriceTV = null;
        public TextView mPriceInfosTV = null;
        public ImageView mPrepareActionIV = null;
        public TextView mValidUntilTV = null;


        public ViewHolder(View v)
        {
            super(v);
            m_baseView = v;
            mNameTV = (TextView)v.findViewById(R.id.nameTV);
            mDescriptionTV = (TextView)v.findViewById(R.id.descriptionTV);
            mPriceTV = (TextView)v.findViewById(R.id.priceTV);
            mPriceInfosTV = (TextView)v.findViewById(R.id.priceInfosTV);
            mPrepareActionIV = (ImageView) v.findViewById(R.id.prepareActionIV);
            mValidUntilTV = (TextView)v.findViewById(R.id.validUntilTV);

            mPrepareActionIV.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null)
            {
                mItemClickListener.onItemClick(null, v, getAdapterPosition(), v.getId());
            }
        }
    }

    public TicketsAdapter(Context argContext, List<Ticket> argTickets)
    {
        super(argContext);
        this.mContext = argContext;
        this.mTickets = argTickets;
    }

    public void setTickets(List<Ticket> argTickets)
    {
        this.mTickets = argTickets;
        notifyDataSetChanged();
    }

    public Ticket getItem(int argPos)
    {
        if (argPos < 0 || argPos >= mTickets.size())
        {
            return new Ticket();
        }

        return mTickets.get(argPos);
    }

    @Override
    public int getItemCount() {
        if (mTickets == null)
        {
            return 0;
        }

        return mTickets.size();
    }

    @Override
    public void onBindViewHolder(TicketsAdapter.ViewHolder holder, int position) {
        Ticket ticket = getItem(position);


        holder.mNameTV.setText(ticket.getName());
        holder.mDescriptionTV.setText(ticket.getDescription());
        holder.mPriceTV.setText(String.format("%.2f CHF", ticket.getPrice()));
        holder.mPrepareActionIV.setTag(position);
        holder.mPriceInfosTV.setText(mContext.getString(R.string.reduced_price));


        if (ticket.isFull())
        {
            holder.mPriceInfosTV.setText(mContext.getString(R.string.full_price));
        }

        holder.mValidUntilTV.setVisibility(View.GONE);
        if (ticket.getDate().after(Calendar.getInstance()))
        {
            /*CardView cv = (CardView)holder.m_baseView;
            cv.setMaxCardElevation(SizeTools.dpToPx(mContext,8));
            cv.setCardElevation(SizeTools.dpToPx(mContext,8));*/
            String dateString;
            if (DateTools.dateAreDifferent(ticket.getDate(), Calendar.getInstance(), DateTools.ComparisonType.OnlyDay))
            {
                dateString = DateTools.dateToString(ticket.getDate());
            }
            else
            {
                dateString = DateTools.dateToString(ticket.getDate(),DateTools.FormatType.OnlyHourWithoutSeconds);
            }
            holder.mValidUntilTV.setText(mContext.getString(R.string.ticket_date_validate,dateString));
            holder.mValidUntilTV.setVisibility(View.VISIBLE);
           // holder.m_baseView.setBackgroundColor(ContextCompat.getColor(mContext,android.R.color.holo_blue_light));
        }

        if (position == 0)
        {
            App app = (App) mContext.getApplicationContext();
            if (app != null && app.isFirstTicket())
            {
                Tutorial ticketTuto = new Tutorial(mContext.getString(R.string.menu_tickets), mContext.getString(R.string.showcase_click_to_prepare_ticket), holder.mPrepareActionIV);
                mTutorialManager.addTutorial(ticketTuto);

                showTuto();

                app.setFirstTicket(false);
            }
        }
    }

    @Override
    public TicketsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ticket, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener argOnItemClickListener)
    {
        this.mItemClickListener = argOnItemClickListener;
    }
}
