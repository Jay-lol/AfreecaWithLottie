package com.jay.josaeworld.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jay.josaeworld.R
import com.jay.josaeworld.databinding.FragmentTeamListBinding
import com.jay.josaeworld.domain.model.response.BroadInfo

class TeamListFragment(
    private val teamNameInfo: List<String>,
    private var teamList: Array<ArrayList<BroadInfo>>?,
    private var secondSujangList: HashMap<String, String>
) : Fragment() {

    private val TAG = "로그 ${this.javaClass.simpleName}"
    private var _binding: FragmentTeamListBinding? = null
    private val binding get() = requireNotNull(_binding)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTeamListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        updateTeamListFragmentUi(teamList)
        teamClickListener()
    }

    private fun teamClickListener() {
        binding.teamFirst.setOnClickListener {
            moveTeamList(
                binding.teamOne.text as String, teamList?.get(0),
                secondSujangList[binding.teamOne.text as String] ?: "1"
            )
        }

        binding.teamSecond.setOnClickListener {
            moveTeamList(
                binding.teamTwo.text as String, teamList?.get(1),
                secondSujangList[binding.teamTwo.text as String] ?: "2"
            )
        }
        binding.teamThird.setOnClickListener {
            moveTeamList(
                binding.teamThree.text as String, teamList?.get(2),
                secondSujangList[binding.teamThree.text as String] ?: "3"
            )
        }
        binding.teamFourth.setOnClickListener {
            moveTeamList(
                binding.teamFour.text as String, teamList?.get(3),
                secondSujangList[binding.teamFour.text as String] ?: "3"
            )
        }
    }

    /**
     * 새로운 액티비티에서 해당 팀 보여주기
     */
    private fun moveTeamList(
        teamName: String,
        teamList: ArrayList<BroadInfo>?,
        secondSujang: String
    ) {
        val intent = Intent(requireContext(), BroadCastActivity::class.java)
        try {
            if (!teamList.isNullOrEmpty()) {
                intent.putExtra("teamName", teamName)
                intent.putExtra("teamInfo", teamList)
                intent.putExtra(
                    "secondSujang",
                    secondSujang
                )

                startActivity(intent)
                // 새로운 액티비티ani, 기존 액티비티ani
                requireActivity().overridePendingTransition(
                    R.anim.slide_from_right,
                    R.anim.slide_to_left
                )
            } else
                (requireActivity() as? MainActivity)?.showError(3)
        } catch (e: Exception) {
            Log.d(TAG, "MainActivity ~ $e() called")
            (requireActivity() as? MainActivity)?.showToast("밑으로 내려서 다시 로딩해 주세요", true)
        }
    }

    fun updateTeamListFragmentUi(mainBJDataList: Array<ArrayList<BroadInfo>>?) {
        mainBJDataList ?: return
        val nextOff = listOf(
            binding.teamOnelotti,
            binding.teamTwolotti,
            binding.teamThreelotti,
            binding.teamFourlotti
        )
        val nextOn = listOf(
            binding.teamOnelottiOn,
            binding.teamTwolottiOn,
            binding.teamThreelottiOn,
            binding.teamFourlottiOn
        )
        val nextViewCnt = listOf(
            binding.viewCntTeam1, binding.viewCntTeam2, binding.viewCntTeam3, binding.viewCntTeam4
        )

        val teamName = listOf(
            binding.teamOne, binding.teamTwo, binding.teamThree, binding.teamFour
        )

        val teamCardViewList =
            listOf(binding.teamFirst, binding.teamSecond, binding.teamThird, binding.teamFourth)

        for ((index, team) in mainBJDataList.withIndex()) {
            // team 정보아니면 skip
            if (team[0].bid == "superbsw123") {
                break
            }

            // 팀 처리
            var onOff = false
            var viewCnt = 0

            val teamNum = mainBJDataList[index][0].teamCode

            teamName[index].text = teamNameInfo[teamNum]

            if (teamName[index].text == "X") {
                teamCardViewList[index].visibility = View.GONE
            } else {
                teamCardViewList[index].visibility = View.VISIBLE
            }

            for (member in team) {
                if (member.onOff == 1) {
                    onOff = true
                    viewCnt += member.viewCnt.filter { it.isDigit() }.toInt()
                }
            }

            if (onOff) {
                val viewer = viewCnt.toString()
                nextViewCnt[index].text = if (viewer.length > 3)
                    viewer.slice(0 until viewer.length - 3) + "," + viewer.slice(viewer.length - 3 until viewer.length)
                else
                    viewer

                nextOff[index].visibility = View.GONE
                nextOff[index].pauseAnimation()
                nextOn[index].visibility = View.VISIBLE
                nextOn[index].playAnimation()
            } else {
                nextViewCnt[index].text = "0"
                nextOn[index].visibility = View.GONE
                nextOn[index].pauseAnimation()
                nextOff[index].visibility = View.VISIBLE
                nextOff[index].playAnimation()
            }
        }
    }
}
