package com.iskollect.controller;

import com.iskollect.model.Reward;
import com.iskollect.model.Student;

import java.math.BigDecimal;
import java.time.LocalDateTime;

final class PopupContext {
    private static Student registeredStudent;
    private static int submittedBottles;
    private static BigDecimal awardedPoints;
    private static BigDecimal currentBalance;
    private static Reward reward;
    private static LocalDateTime redemptionTime;
    private static boolean confirmed;

    private PopupContext() {
    }

    static void registration(Student student) {
        registeredStudent = student;
    }

    static Student registeredStudent() {
        return registeredStudent;
    }

    static void bottleSubmission(int bottles, BigDecimal points, BigDecimal balance) {
        submittedBottles = bottles;
        awardedPoints = points;
        currentBalance = balance;
    }

    static int submittedBottles() {
        return submittedBottles;
    }

    static BigDecimal awardedPoints() {
        return awardedPoints;
    }

    static BigDecimal currentBalance() {
        return currentBalance;
    }

    static void redemptionConfirmation(Reward selectedReward) {
        reward = selectedReward;
        confirmed = false;
    }

    static void confirmRedemption() {
        confirmed = true;
    }

    static boolean redemptionConfirmed() {
        return confirmed;
    }

    static Reward reward() {
        return reward;
    }

    static void redemptionReceipt(
        Reward redeemedReward,
        BigDecimal balance,
        LocalDateTime occurredAt
    ) {
        reward = redeemedReward;
        currentBalance = balance;
        redemptionTime = occurredAt;
    }

    static LocalDateTime redemptionTime() {
        return redemptionTime;
    }
}
