package com.smusic.echo.fragments


import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import com.smusic.echo.CurrentSongHelper
import com.smusic.echo.R
import com.smusic.echo.Songs
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.random.nextInt



class SongPlayingFragment : Fragment() {
    var myActivity: Activity? = null
    var mediaplayer: MediaPlayer? = null
    var startTimeText: TextView? = null
    var endTimedText: TextView? = null
    var playpauseImageButton: ImageButton? = null
    var previousImageButton: ImageButton? = null
    var nextImageButton: ImageButton? = null
    var loopImageButton: ImageButton? = null
    var seekbar: SeekBar? = null
    var songArtistView: TextView? = null
    var songTitleView: TextView? = null
    var shuffleImageButton: ImageButton? = null

    var currentPosition: Int = 0
    var fetchSongs: ArrayList<Songs>? = null
    var currentSongHelper: CurrentSongHelper? = null


    var updateSongTime = object: Runnable{
        override fun run() {
            val getcurrent = mediaplayer?.currentPosition
            startTimeText?.setText(String.format("%d:%d",
                TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long),
                TimeUnit.MILLISECONDS.toSeconds(getcurrent?.toLong() as Long) -
                         TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(getcurrent?.toLong() as Long))))

            Handler().postDelayed(this, 1000)
        }

    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_song_playing, container, false)
        seekbar = view?.findViewById(R.id.seekBar)
        startTimeText = view?.findViewById(R.id.startTime)
        endTimedText = view?.findViewById(R.id.endTime)
        playpauseImageButton = view?.findViewById(R.id.playPauseButton)
        previousImageButton = view?.findViewById(R.id.previousButton)
        nextImageButton = view?.findViewById(R.id.nextButton)
        loopImageButton = view?.findViewById(R.id.loopButton)
        songArtistView = view?.findViewById(R.id.songArtist)
        songTitleView = view?.findViewById(R.id.songTitle)
        shuffleImageButton = view?.findViewById(R.id.shuffleButton)

        return view

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        myActivity = context as Activity
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        myActivity = activity
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        currentSongHelper = CurrentSongHelper()
        currentSongHelper?.isPlaying = true
        currentSongHelper?.isLoop = false
        currentSongHelper?.isShuffle = false

        var path: String? = null
        var _songTitle: String? = null
        var _songArtist: String? = null
        var songId: Long = 0
        try {
            path = arguments?.getString("path")
            _songTitle = arguments?.getString("songTitle")
            _songArtist = arguments?.getString("songArtist")
            songId = arguments?.getInt("SongId")!!.toLong()

            currentPosition = arguments!!.getInt("songPosition")
            fetchSongs = arguments!!.getParcelableArrayList("songData")


            currentSongHelper?.songPath = path
            currentSongHelper?.songTitle = _songTitle
            currentSongHelper?.songArtist = _songArtist
            currentSongHelper?.songId = songId
            currentSongHelper?.currentPosition = currentPosition

            updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaplayer = MediaPlayer()
        mediaplayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        try {
            mediaplayer?.setDataSource(myActivity as Context, Uri.parse(path))
            mediaplayer?.prepare()

        } catch (e: Exception) {
            e.printStackTrace()

        }
        mediaplayer?.start()
        processInformation(mediaplayer as MediaPlayer)


        if (currentSongHelper?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        } else {
            playpauseImageButton?.setBackgroundResource((R.drawable.pause_icon))
        }
        mediaplayer?.setOnCompletionListener {
            onSongComplete()
        }
        clickHandler()
    }

    fun clickHandler() {
        shuffleImageButton?.setOnClickListener({
            if (currentSongHelper?.isShuffle as Boolean){
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
                currentSongHelper?.isShuffle = false
            }else{
                currentSongHelper?.isShuffle = true
                currentSongHelper?.isLoop = false
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_icon)
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
        })
        nextImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isShuffle as Boolean) {
                playNext("PlayNextLikeNormalShuffle")
            } else {
                playNext("PlayNextNormal")
            }

        })
        previousImageButton?.setOnClickListener({
            currentSongHelper?.isPlaying = true
            if (currentSongHelper?.isShuffle as Boolean){
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }
            playPrevious()
        })
        loopImageButton?.setOnClickListener({
            if (currentSongHelper?.isLoop as Boolean){
                currentSongHelper?.isLoop = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_white_icon)
            }else{
                currentSongHelper?.isLoop = true
                currentSongHelper?.isShuffle = false
                loopImageButton?.setBackgroundResource(R.drawable.loop_icon)
                shuffleImageButton?.setBackgroundResource(R.drawable.shuffle_white_icon)
            }
        })
        playpauseImageButton?.setOnClickListener({
            if (mediaplayer?.isPlaying as Boolean) {
                mediaplayer?.pause()
                currentSongHelper?.isPlaying = false
                playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
            } else {
                mediaplayer?.start()
                currentSongHelper?.isPlaying = true
                playpauseImageButton?.setBackgroundResource((R.drawable.pause_icon))
            }

        })
    }

    fun playNext(check: String) {
        if (check.equals("PlayNextNormal", true)) {
            currentPosition = currentPosition + 1
        } else if (check.equals("PlayNextLikeNormalShuffle", true)) {
            var randomObject = Random()
            var randomPosition = randomObject.nextInt(fetchSongs?.size?.plus(1) as Int)
            currentPosition = randomPosition

        }
        if (currentPosition == fetchSongs?.size) {
            currentPosition = 0
        }
        currentSongHelper?.isLoop = false
        var nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songArtist = nextSong?.artist
        currentSongHelper?.songId = nextSong?.songID as Long
        currentSongHelper?.currentPosition = currentPosition

        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        mediaplayer?.reset()
        try {
            mediaplayer?.setDataSource(
                myActivity as Context,
                Uri.parse(currentSongHelper?.songPath)
            )
            mediaplayer?.prepare()
            mediaplayer?.start()
            processInformation(mediaplayer as MediaPlayer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playPrevious() {
        currentPosition = currentPosition - 1
        if (currentPosition == -1) {
            currentPosition = 0
        }
        if (currentSongHelper?.isPlaying as Boolean) {
            playpauseImageButton?.setBackgroundResource(R.drawable.pause_icon)
        } else {
            playpauseImageButton?.setBackgroundResource(R.drawable.play_icon)
        }
        currentSongHelper?.isLoop = false
        val nextSong = fetchSongs?.get(currentPosition)
        currentSongHelper?.songTitle = nextSong?.songTitle
        currentSongHelper?.songPath = nextSong?.songData
        currentSongHelper?.songId = nextSong?.songID as Long
        currentSongHelper?.currentPosition = currentPosition

        updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

        mediaplayer?.reset()
        try {
            mediaplayer?.setDataSource(activity as Context, Uri.parse(currentSongHelper?.songPath))
            mediaplayer?.prepare()
            mediaplayer?.start()

            processInformation(mediaplayer as MediaPlayer)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun onSongComplete(){
        if (currentSongHelper?.isShuffle as Boolean){
            playNext("PlayNextLikeNormalShuffle")
            currentSongHelper?.isPlaying = true
        }else{
            if (currentSongHelper?.isLoop as Boolean){

            currentSongHelper?.isPlaying = true
                var nextSong = fetchSongs?.get(currentPosition)

                currentSongHelper?.songTitle = nextSong?.songTitle
                currentSongHelper?.songPath = nextSong?.songData
                currentSongHelper?.currentPosition = currentPosition
                currentSongHelper?.songId = nextSong?.songID as Long

                updateTextViews(currentSongHelper?.songTitle as String, currentSongHelper?.songArtist as String)

                mediaplayer?.reset()

                try {
                    mediaplayer?.setDataSource(myActivity as Context, Uri.parse(currentSongHelper?.songPath))
                    mediaplayer?.prepare()
                    mediaplayer?.start()

                    processInformation(mediaplayer as MediaPlayer)

                }catch (e: Exception){
                    e.printStackTrace()
                }



            }else{
                playNext("PlayNextNormal")
                currentSongHelper?.isPlaying = true
            }
        }
    }

    fun updateTextViews(songtitle: String, songArtist: String){
        songTitleView?.setText(songtitle)
        songArtistView?.setText(songArtist)
    }

    fun processInformation(mediaPlayer: MediaPlayer){
        val finalTime = mediaPlayer.duration
        val startTime = mediaPlayer.currentPosition
        seekbar?.max = finalTime
        startTimeText?.setText(String.format("%d: %d",
            TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long),
            TimeUnit.MILLISECONDS.toSeconds(startTime?.toLong() as Long) -
                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime?.toLong() as Long))))
        endTimedText?.setText(String.format("%d: %d",
            TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long),
            TimeUnit.MILLISECONDS.toSeconds(finalTime?.toLong() as Long) -
                    TimeUnit.MILLISECONDS.toSeconds(TimeUnit.MILLISECONDS.toMinutes(finalTime?.toLong() as Long))))

        seekbar?.setProgress(startTime)
        Handler().postDelayed(updateSongTime, 1000)
    }


}
