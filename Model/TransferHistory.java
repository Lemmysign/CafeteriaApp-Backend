package Evercare_CafeteriaApp.Model;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "transfer_History")

public class TransferHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transferId;
    private Long senderId;
    private Long receiverId;
    private BigDecimal amountSent;
    private BigDecimal amountReceived;

    public TransferHistory(Long transferId, Long senderId, Long receiverId, BigDecimal amountSent, BigDecimal amountReceived) {
        this.transferId = transferId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.amountSent = amountSent;
        this.amountReceived = amountReceived;
    }

    public TransferHistory() {
    }

    public Long getTransferId() {
        return transferId;
    }

    public void setTransferId(Long transferId) {
        this.transferId = transferId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmountSent() {
        return amountSent;
    }

    public void setAmountSent(BigDecimal amountSent) {
        this.amountSent = amountSent;
    }

    public BigDecimal getAmountReceived() {
        return amountReceived;
    }

    public void setAmountReceived(BigDecimal amountReceived) {
        this.amountReceived = amountReceived;
    }
}
