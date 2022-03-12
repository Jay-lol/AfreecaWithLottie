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
import com.jay.josaeworld.domain.goodString
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
        binding.firstTeam.root.setOnClickListener {
            moveTeamList(
                binding.firstTeam.teamName.text as String, teamList?.get(0),
                secondSujangList[binding.firstTeam.teamName.text as String] ?: "1"
            )
        }

        binding.secondTeam.root.setOnClickListener {
            moveTeamList(
                binding.secondTeam.teamName.text as String, teamList?.get(1),
                secondSujangList[binding.secondTeam.teamName.text as String] ?: "2"
            )
        }
        binding.thirdTeam.root.setOnClickListener {
            moveTeamList(
                binding.thirdTeam.teamName.text as String, teamList?.get(2),
                secondSujangList[binding.thirdTeam.teamName.text as String] ?: "3"
            )
        }
        binding.fourthTeam.root.setOnClickListener {
            moveTeamList(
                binding.fourthTeam.teamName.text as String, teamList?.get(3),
                secondSujangList[binding.fourthTeam.teamName.text as String] ?: "3"
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
        val teamBJDataList = mainBJDataList.clone().dropLast(1)

        val viewTeamDataList = listOf(
            binding.firstTeam,
            binding.secondTeam,
            binding.thirdTeam,
            binding.fourthTeam
        )

        for ((index, team) in teamBJDataList.withIndex()) {

            val teamView = viewTeamDataList[index]

            var onOff = false
            var viewCnt = 0

            val teamNum = teamBJDataList[index][0].teamCode

            teamView.teamName.text = teamNameInfo[teamNum]

            if (teamView.teamName.text == "X") {
                teamView.root.visibility = View.GONE
            } else {
                teamView.root.visibility = View.VISIBLE
            }

            for (member in team) {
                if (member.onOff == 1) {
                    onOff = true
                    viewCnt += member.viewCnt.filter { it.isDigit() }.toInt()
                }
            }

            if (onOff) {
                teamView.teamViewCnt.text = viewCnt.toString().goodString()
                teamView.teamBroadOnLottie.visibility = View.VISIBLE
                teamView.teamBroadOnLottie.playAnimation()
                teamView.teamBroadOffLottie.visibility = View.GONE
                teamView.teamBroadOffLottie.pauseAnimation()
            } else {
                teamView.teamViewCnt.text = "0"
                teamView.teamBroadOnLottie.visibility = View.GONE
                teamView.teamBroadOnLottie.pauseAnimation()
                teamView.teamBroadOffLottie.visibility = View.VISIBLE
                teamView.teamBroadOffLottie.playAnimation()
            }
        }
    }
}
