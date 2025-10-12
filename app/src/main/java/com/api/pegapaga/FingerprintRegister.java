package com.api.pegapaga;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class FingerprintRegister extends AppCompatActivity {

    private HorizontalScrollView hScroll;
    private LinearLayout dotsLayout, container;
    private int sectionCount = 5;
    private int currentIndex = 0;
    private int screenWidth;
    private boolean userScrolling = false;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_register);

        hScroll = findViewById(R.id.hScroll);
        dotsLayout = findViewById(R.id.dotsLayout);
        container = findViewById(R.id.container);

        hScroll.setSmoothScrollingEnabled(true);
        screenWidth = getResources().getDisplayMetrics().widthPixels;

        // Ajusta cada seção para ocupar toda a tela
        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                container.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                for (int i = 0; i < container.getChildCount(); i++) {
                    View child = container.getChildAt(i);
                    ViewGroup.LayoutParams params = child.getLayoutParams();
                    params.width = screenWidth;
                    child.setLayoutParams(params);
                }
            }
        });

        updateDots(0);

        // Detecta quando o usuário rola
        hScroll.getViewTreeObserver().addOnScrollChangedListener(() -> {
            userScrolling = true;
            handler.removeCallbacks(snapRunnable);
            handler.postDelayed(snapRunnable, 30); // espera 150ms sem scroll para "travar"
        });
    }

    // Runnable que faz o snap automático
    private final Runnable snapRunnable = new Runnable() {
        @Override
        public void run() {
            if (userScrolling) {
                userScrolling = false;
                int scrollX = hScroll.getScrollX();
                int targetIndex = Math.round((float) scrollX / screenWidth);
                smoothScrollToSection(targetIndex);
            }
        }
    };

    private void smoothScrollToSection(int index) {
        currentIndex = Math.max(0, Math.min(index, sectionCount - 1));
        int targetX = currentIndex * screenWidth;
        hScroll.smoothScrollTo(targetX, 0);
        updateDots(currentIndex);
    }

    // Atualiza os pontinhos
    private void updateDots(int activeIndex) {
        for (int i = 0; i < sectionCount; i++) {
            View dot = dotsLayout.getChildAt(i);
            if (dot != null) {
                dot.setBackgroundResource(i == activeIndex
                        ? R.drawable.dot_active
                        : R.drawable.dot_inactive);
            }
        }
    }
}