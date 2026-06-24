package com.iskollect.controller;

import com.iskollect.model.Reward;

final class RewardEditorContext {
    private static Reward selected;

    private RewardEditorContext() {
    }

    static void select(Reward reward) {
        selected = reward;
    }

    static Reward selected() {
        return selected;
    }
}
