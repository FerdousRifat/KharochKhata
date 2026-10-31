package com.kharochkhata.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * Connects the list of Expense objects to the RecyclerView.
 *
 * RecyclerView only creates as many row views as fit on the screen and
 * re-uses them while scrolling. The ViewHolder keeps references to the
 * views of one row so findViewById() is not called again and again.
 */
public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {

    /** The Activity decides what happens on a tap or on the delete button. */
    public interface OnExpenseActionListener {
        void onExpenseClick(Expense expense);

        void onExpenseDelete(Expense expense);
    }

    private final List<Expense> expenses;
    private final OnExpenseActionListener listener;

    public ExpenseAdapter(List<Expense> expenses, OnExpenseActionListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);

        holder.tvTitle.setText(expense.getTitle());
        holder.tvCategoryDate.setText(expense.getCategory() + "  •  "
                + AppUtils.formatDate(expense.getDateMillis()));
        holder.tvAmount.setText(AppUtils.formatMoney(holder.itemView.getContext(), expense.getAmount()));

        boolean hasPhoto = expense.getPhoto() != null && !expense.getPhoto().isEmpty();
        holder.ivHasPhoto.setVisibility(hasPhoto ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onExpenseClick(expense));
        holder.btnDelete.setOnClickListener(v -> listener.onExpenseDelete(expense));
    }

    @Override
    public int getItemCount() {
        return expenses.size();
    }

    /** Holds the views of one row (item_expense.xml). */
    static class ExpenseViewHolder extends RecyclerView.ViewHolder {
        final TextView tvTitle;
        final TextView tvCategoryDate;
        final TextView tvAmount;
        final ImageView ivHasPhoto;
        final ImageButton btnDelete;

        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvItemTitle);
            tvCategoryDate = itemView.findViewById(R.id.tvItemCategoryDate);
            tvAmount = itemView.findViewById(R.id.tvItemAmount);
            ivHasPhoto = itemView.findViewById(R.id.ivItemHasPhoto);
            btnDelete = itemView.findViewById(R.id.btnItemDelete);
        }
    }
}
