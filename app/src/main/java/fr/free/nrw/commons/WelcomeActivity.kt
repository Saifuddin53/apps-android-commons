package fr.free.nrw.commons

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import fr.free.nrw.commons.databinding.ActivityWelcomeBinding
import fr.free.nrw.commons.databinding.PopupForCopyrightBinding
import fr.free.nrw.commons.quiz.QuizActivity
import fr.free.nrw.commons.theme.BaseActivity
import fr.free.nrw.commons.utils.ConfigUtils

class WelcomeActivity : BaseActivity() {

    private lateinit var binding: ActivityWelcomeBinding
    private var copyrightBinding: PopupForCopyrightBinding? = null

    private val adapter = WelcomePagerAdapter()
    private var isQuiz: Boolean = false
    private var dialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isQuiz = intent?.extras?.getBoolean("isQuiz") ?: false

        if (ConfigUtils.isBetaFlavour()) {
            binding.finishTutorialButton.visibility = View.VISIBLE
            val builder = AlertDialog.Builder(this)
            copyrightBinding = PopupForCopyrightBinding.inflate(layoutInflater)
            builder.setView(copyrightBinding!!.root)
            builder.setCancelable(false)
            dialog = builder.create().apply { show() }
            copyrightBinding!!.buttonOk.setOnClickListener { dialog?.dismiss() }
        }

        binding.welcomePager.adapter = adapter
        binding.welcomePagerIndicator.setViewPager(binding.welcomePager)
        binding.finishTutorialButton.setOnClickListener { finishTutorial() }
    }

    override fun onDestroy() {
        if (isQuiz) {
            startActivity(Intent(this, QuizActivity::class.java))
        }
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (binding.welcomePager.currentItem != 0) {
            binding.welcomePager.setCurrentItem(binding.welcomePager.currentItem - 1, true)
        } else {
            if (defaultKvStore.getBoolean("firstrun", true)) {
                finishAffinity()
            } else {
                super.onBackPressed()
            }
        }
    }

    fun finishTutorial() {
        defaultKvStore.putBoolean("firstrun", false)
        finish()
    }

    companion object {
        fun startYourself(context: Context) {
            context.startActivity(Intent(context, WelcomeActivity::class.java))
        }
    }
}
