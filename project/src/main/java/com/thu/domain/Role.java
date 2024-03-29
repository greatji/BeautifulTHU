package com.thu.domain;

import javax.persistence.*;

/**
 * Created by JasonLee on 16/12/2.
 */
@Entity
public class Role {
    @Id
    private String role;
    private String displayName;
    private String respPerson;
    private Boolean editContacts = false;
    private Boolean refuseQuestion = false;
    private Boolean respondQuestion = false;
    private Boolean devolveQuestion = false;
    private Boolean classifyQuestion = false;
    private Boolean delayDdl = false;
    private Boolean approveResponse = false;
    private Long receivedNumber = 0L;
    private Long ontimeNumber = 0L;
    private Long overtimeNumber = 0L;
    private Long directRespondNumber = 0L;
    private Long goodNumber = 0L;
    private Long badNumber = 0L;
    private Boolean byZongban = false;

    protected Role() {}

    public Role(String role, String displayName, String respPerson) {
        this.role = role;
        this.displayName = displayName;
        this.respPerson = respPerson;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRespPerson() {
        return respPerson;
    }

    public void setRespPerson(String respPerson) {
        this.respPerson = respPerson;
    }

    public Boolean getEditContacts() {
        return editContacts;
    }

    public void setEditContacts(Boolean editContacts) {
        this.editContacts = editContacts;
    }

    public Boolean getRefuseQuestion() {
        return refuseQuestion;
    }

    public void setRefuseQuestion(Boolean refuseQuestion) {
        this.refuseQuestion = refuseQuestion;
    }

    public Boolean getRespondQuestion() {
        return respondQuestion;
    }

    public void setRespondQuestion(Boolean respondQuestion) {
        this.respondQuestion = respondQuestion;
    }

    public Boolean getDevolveQuestion() {
        return devolveQuestion;
    }

    public void setDevolveQuestion(Boolean devolveQuestion) {
        this.devolveQuestion = devolveQuestion;
    }

    public Boolean getClassifyQuestion() {
        return classifyQuestion;
    }

    public void setClassifyQuestion(Boolean classifyQuestion) {
        this.classifyQuestion = classifyQuestion;
    }

    public Boolean getDelayDdl() {
        return delayDdl;
    }

    public void setDelayDdl(Boolean delayDdl) {
        this.delayDdl = delayDdl;
    }

    public Boolean getApproveResponse() {
        return approveResponse;
    }

    public void setApproveResponse(Boolean approveResponse) {
        this.approveResponse = approveResponse;
    }

    public Long getReceivedNumber() {
        return receivedNumber;
    }

    public void setReceivedNumber(Long receivedNumber) {
        this.receivedNumber = receivedNumber;
    }

    public Long getOntimeNumber() {
        return ontimeNumber;
    }

    public void setOntimeNumber(Long ontimeNumber) {
        this.ontimeNumber = ontimeNumber;
    }

    public Long getOvertimeNumber() {
        return overtimeNumber;
    }

    public void setOvertimeNumber(Long overtimeNumber) {
        this.overtimeNumber = overtimeNumber;
    }

    public Long getDirectRespondNumber() {
        return directRespondNumber;
    }

    public void setDirectRespondNumber(Long directRespondNumber) {
        this.directRespondNumber = directRespondNumber;
    }

    public Long getGoodNumber() {
        return goodNumber;
    }

    public void setGoodNumber(Long goodNumber) {
        this.goodNumber = goodNumber;
    }

    public Long getBadNumber() {
        return badNumber;
    }

    public void setBadNumber(Long badNumber) {
        this.badNumber = badNumber;
    }

    public Boolean getByZongban() {
        return byZongban;
    }

    public void setByZongban(Boolean byZongban) {
        this.byZongban = byZongban;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role1 = (Role) o;

        return getRole() != null ? getRole().equals(role1.getRole()) : role1.getRole() == null;

    }

    @Override
    public int hashCode() {
        return getRole() != null ? getRole().hashCode() : 0;
    }
}
