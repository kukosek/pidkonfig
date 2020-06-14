package com.example.pidkonfig.ui.main

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.RadioGroup
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.example.pidkonfig.MainActivity
import com.example.pidkonfig.R
import com.example.pidkonfig.Setting
import kotlin.math.floor


/**
 * A placeholder fragment
 */
class PlaceholderFragment : Fragment() {
    data class SlRange(val min: Float, val max: Float)
    var sharedPref : SharedPreferences? = null
    fun getRange(key: String) : SlRange {
        val defVal =  "0 - 1"
        var tmpStRng = sharedPref?.getString(key, defVal)?.split(" - ")
        if (tmpStRng == null || tmpStRng.size  < 2) {
            tmpStRng = defVal.split(" - ")
        }
        return SlRange(tmpStRng[0].trim().toFloat(), tmpStRng[1].trim().toFloat())
    }
    data class Ranges(val Kp: SlRange, val Kd: SlRange, val Ki: SlRange, val K_lineLost: SlRange, val target_linepos: SlRange)
    var ranges : Ranges? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
        val sn = arguments?.getInt(ARG_SECTION_NUMBER)
        ranges = when (sn) {
            2 -> Ranges(getRange("range_kp"), getRange("range_kd"), getRange("range_ki"), getRange("range_k_linelost"), getRange("range_target_linepos"))
            else -> Ranges(getRange("range_mkp"), getRange("range_kd"), getRange("range_mki"), SlRange(0.0f,1.1f), SlRange(0.0f,1.1f))
        }

        when (sn) {

        }
    }

    val sliderStep = 0.0001
    val sliderMax : Int =  (1/sliderStep).toInt()

    lateinit var root : View
    lateinit var setting : Setting
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        setting = (activity as MainActivity).setting
        val comHandler = (activity as MainActivity).comHandler


        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)




        val do_riti : Int

        val sn = arguments?.getInt(ARG_SECTION_NUMBER)
        when(sn) {
            2 -> {
                root = inflater.inflate(R.layout.fragment_linepid, container, false)
                ranges = Ranges(getRange("range_kp"), getRange("range_kd"), getRange("range_ki"), getRange("range_k_linelost"), getRange("range_target_linepos"))
            }
            3 -> {
                root = inflater.inflate(R.layout.fragment_motors, container, false)
                ranges = Ranges(getRange("range_mkp"), getRange("range_kd"), getRange("range_mki"), SlRange(0.0f,1.1f), SlRange(0.0f,1.1f))
            }
            4 -> {
                root = inflater.inflate(R.layout.fragment_mode, container, false)
            }
            else ->{
                root = inflater.inflate(R.layout.fragment_presets, container, false)
            }
        }
        class sbListener : OnSeekBarChangeListener {
            var lock = false
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                lock = true
                var convValue : Float

                fun convertSliderValue(value: Int, range: SlRange?) : Float{
                    return (value - 0) * (range!!.max - range!!.min) / (sliderMax - 0) + range!!.min
                }

                fun formatSendVal(float: Float) : String {
                    val cvs = float.toString()
                    if (cvs.length > 8) {
                        return cvs.substring(0, 7)
                    }
                    return cvs
                }

                when (seekBar.id) {

                    R.id.seekbar_Kp -> {
                        convValue = convertSliderValue(progress, ranges?.Kp)
                        root.findViewById<TextView>(R.id.value_Kp).text = "" + convValue
                        setting.Kp = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("lkp=$cvs\n")
                    }
                    R.id.seekbar_Kd -> {
                        convValue = convertSliderValue(progress ,ranges?.Kd);
                        root.findViewById<TextView>(R.id.value_Kd).text = "" + convValue
                        setting.Kd = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("lkd=$cvs\n")
                    }
                    R.id.seekbar_Ki -> {
                        convValue = convertSliderValue(progress, ranges?.Ki)
                        root.findViewById<TextView>(R.id.value_Ki).text = "" + convValue
                        setting.Ki = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("lki=$cvs\n")
                    }
                    R.id.seekbar_K_lineLost -> {
                        convValue = convertSliderValue(progress, ranges?.K_lineLost)
                        root.findViewById<TextView>(R.id.value_K_lineLost).text = "" + convValue
                        setting.K_lineLost = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("lkl=$cvs\n")
                    }
                    R.id.seekbar_target_linepos -> {
                        convValue = convertSliderValue(progress, ranges?.target_linepos)
                        root.findViewById<TextView>(R.id.value_target_linepos).text = "" + convValue
                        setting.target_linepos = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("ltp=$cvs\n")
                    }
                    R.id.seekbar_baseSpeed -> {
                        convValue = progress.toFloat()/sliderMax.toFloat()
                        root.findViewById<TextView>(R.id.value_baseSpeed).text = "" + convValue
                        setting.mbs = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("mbs=$cvs\n")
                    }
                    R.id.seekbar_mKp -> {
                        convValue = convertSliderValue(progress, ranges?.Kp)
                        root.findViewById<TextView>(R.id.value_mKp).text = "" + convValue
                        setting.mKp = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("mkp=$cvs\n")
                    }
                    R.id.seekbar_mKi -> {
                        convValue = convertSliderValue(progress, ranges?.Ki)
                        root.findViewById<TextView>(R.id.value_mKi).text = "" + convValue
                        setting.mKi = convValue
                        val cvs = formatSendVal(convValue)
                        comHandler?.send("mki=$cvs\n")
                    }
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        }




        when(sn) {
            2 -> {
                with(root.findViewById<SeekBar>(R.id.seekbar_Kp)) {
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax

                }
                with(root.findViewById<SeekBar>(R.id.seekbar_Kd)){
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
                with(root.findViewById<SeekBar>(R.id.seekbar_Ki)){
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
                with(root.findViewById<SeekBar>(R.id.seekbar_K_lineLost)){
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
                with(root.findViewById<SeekBar>(R.id.seekbar_target_linepos)){
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
            }
            3 -> {
                with(root.findViewById<SeekBar>(R.id.seekbar_mKp)) {
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
                with(root.findViewById<SeekBar>(R.id.seekbar_mKi)) {
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
                with(root.findViewById<SeekBar>(R.id.seekbar_baseSpeed)) {
                    setOnSeekBarChangeListener(sbListener())
                    max = sliderMax
                }
            }
            4 -> {
                root.findViewById<RadioGroup>(R.id.modes).setOnCheckedChangeListener { _, checkedId -> // checkedId is the RadioButton selected
                    // Check which radio button was clicked
                    val modeid = when (checkedId) {
                        R.id.mode_pid -> 1
                        R.id.mode_motortest -> 2
                        else -> 0
                    }


                    comHandler?.send("rtm=$modeid\n")
                }
            }
            else -> {

            }
        }
        return root
    }
    fun onSettingChange() {
        if (view !=null){
            val sn =arguments?.getInt(ARG_SECTION_NUMBER)

            fun valueToSlider(value: Float, range : SlRange?) : Int {
                return ((value - range!!.min) * (sliderMax) / (range!!.max - range!!.min)).toInt();
            }

            when (sn) {
                1 -> {

                }
                2 -> {
                    root.findViewById<CheckBox>(R.id.checkBox_Kp).isChecked = setting.Kp != 0.0f
                    root.findViewById<SeekBar>(R.id.seekbar_Kp).progress = valueToSlider(setting.Kp, ranges?.Kp)

                    root.findViewById<CheckBox>(R.id.checkBox_Kd).isChecked = setting.Kd != 0.0f
                    root.findViewById<SeekBar>(R.id.seekbar_Kd).progress = valueToSlider(setting.Kd, ranges?.Kd)

                    root.findViewById<CheckBox>(R.id.checkBox_Ki).isChecked = setting.Ki != 0.0f
                    root.findViewById<SeekBar>(R.id.seekbar_Ki).progress = valueToSlider(setting.Ki, ranges?.Ki)

                    root.findViewById<SeekBar>(R.id.seekbar_K_lineLost).progress = valueToSlider(setting.K_lineLost, ranges?.K_lineLost)
                    root.findViewById<SeekBar>(R.id.seekbar_target_linepos).progress = valueToSlider(setting.target_linepos, ranges?.target_linepos)
                }
                3 -> {
                    root.findViewById<SeekBar>(R.id.seekbar_baseSpeed).progress = valueToSlider(setting.mbs, SlRange(0.0f, 1.0f))

                    root.findViewById<CheckBox>(R.id.checkBox_m_pidEnable).isChecked = setting.M_pidEnable

                    root.findViewById<CheckBox>(R.id.checkBox_mKp).isChecked = setting.mKp != 0.0f
                    root.findViewById<SeekBar>(R.id.seekbar_mKp).progress = valueToSlider(setting.mKp, ranges?.Kp)

                    root.findViewById<CheckBox>(R.id.checkBox_mKi).isChecked = setting.mKi != 0.0f
                    root.findViewById<SeekBar>(R.id.seekbar_mKi).progress = valueToSlider(setting.mKi, ranges?.Ki)
                }
                4 -> {

                }
                else -> {

                }
            }
        }

    }
    fun checkBoxToggle(view: View) : Unit {
        val cb = view as CheckBox;
        val value = when(cb.isChecked()) {
            true -> 1
            false -> 0
        }
        val id = cb.id

    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private const val ARG_SECTION_NUMBER = "section_number"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        @JvmStatic
        fun newInstance(sectionNumber: Int): PlaceholderFragment {
            return PlaceholderFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_SECTION_NUMBER, sectionNumber)
                }
            }
        }
    }
}