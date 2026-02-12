package com.recovery.recovery_ai

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private val handler = Handler(Looper.getMainLooper())

    private val homeDelayMs = 2000L
    private val slideDelayMs = 5000L

    // “Just a tad bit slower”
    private val transitionDurationMs = 1700L // try 1500–2000

    private val pages = listOf(
        OnboardingPage(R.layout.home_splash_screen, R.drawable.splash_1),
        OnboardingPage(R.layout.one_a_login_screen, R.drawable.splash_2a),
        OnboardingPage(R.layout.one_b_login_screen, R.drawable.splash_2b_),
        OnboardingPage(R.layout.one_c_login_screen, R.drawable.splash_2c)
    )

    // Bounce direction between 1..3
    private var direction = +1

    // Prevent overlapping animations (common crash cause)
    private var isAutoAnimating = false

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            if (isFinishing || isDestroyed) return
            if (!::viewPager.isInitialized) return
            if (isAutoAnimating) return

            // If pager isn’t ready yet, retry shortly
            if (viewPager.width == 0) {
                handler.postDelayed(this, 100L)
                return
            }

            if (viewPager.scrollState != ViewPager2.SCROLL_STATE_IDLE) {
                handler.postDelayed(this, 150L)
                return
            }

            val current = viewPager.currentItem

            val next = when (current) {
                0 -> 1
                1 -> 2
                2 -> if (direction == +1) 3 else 1
                3 -> 2
                else -> 1
            }

            if (current == 3) direction = -1
            if (current == 1) direction = +1

            slowScrollToItemSafe(next, transitionDurationMs)

            val delayMs = if (current == 0) homeDelayMs else slideDelayMs
            handler.postDelayed(this, delayMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        viewPager = findViewById(R.id.viewPager)
        viewPager.adapter = OnboardingAdapter(pages)
        viewPager.isUserInputEnabled = false

        // Professional crossfade
        viewPager.setPageTransformer { page, position ->
            val p = abs(position)
            page.alpha = (1f - p).coerceIn(0f, 1f)
            page.translationX = -position * page.width
            page.scaleX = 1f
            page.scaleY = 1f
        }

        viewPager.setCurrentItem(0, false)

        // Start after home delay
        handler.postDelayed(autoScrollRunnable, homeDelayMs)
    }

    override fun onResume() {
        super.onResume()
        handler.removeCallbacks(autoScrollRunnable)
        val delayMs = if (viewPager.currentItem == 0) homeDelayMs else slideDelayMs
        handler.postDelayed(autoScrollRunnable, delayMs)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(autoScrollRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoScrollRunnable)
    }

    private fun slowScrollToItemSafe(targetItem: Int, durationMs: Long) {
        val currentItem = viewPager.currentItem
        if (targetItem == currentItem) return
        if (viewPager.width == 0) {
            viewPager.setCurrentItem(targetItem, true)
            return
        }

        // Only 1-step moves in your sequence
        val directionSign = if (targetItem > currentItem) -1f else 1f
        val totalDragDistance = viewPager.width.toFloat() * directionSign

        // If fake drag can’t start, fall back safely
        if (!viewPager.beginFakeDrag()) {
            viewPager.setCurrentItem(targetItem, true)
            return
        }

        isAutoAnimating = true

        val animator = ValueAnimator.ofFloat(0f, totalDragDistance).apply {
            duration = durationMs
            interpolator = AccelerateDecelerateInterpolator()

            var lastValue = 0f

            addUpdateListener { va ->
                val value = va.animatedValue as Float
                val delta = value - lastValue
                lastValue = value

                // Guard: if fake-drag ended unexpectedly, stop
                if (!viewPager.isFakeDragging) {
                    cancel()
                    return@addUpdateListener
                }

                viewPager.fakeDragBy(delta)
            }

            addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}

                override fun onAnimationEnd(animation: android.animation.Animator) {
                    endFakeDragAndSnap(targetItem)
                }

                override fun onAnimationCancel(animation: android.animation.Animator) {
                    endFakeDragAndSnap(targetItem)
                }

                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })
        }

        animator.start()
    }

    private fun endFakeDragAndSnap(targetItem: Int) {
        try {
            if (viewPager.isFakeDragging) {
                viewPager.endFakeDrag()
            }
        } catch (_: Throwable) {

        } finally {
            // Ensure we land exactly on the target page
            viewPager.setCurrentItem(targetItem, false)
            isAutoAnimating = false
        }
    }
}
