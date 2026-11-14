package com.androidbook.bookmarket.mypage


import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.*
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import com.androidbook.bookmarket.R
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class PaintActivity : AppCompatActivity() {

   lateinit var drawingView: DrawingView


    lateinit var backgroundIv: ImageView

    lateinit var pickMedia : ActivityResultLauncher<PickVisualMediaRequest>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.paint)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)


        drawingView = findViewById(R.id.drawing_view)

        backgroundIv = findViewById(R.id.background_iv)

        val btnRed :ImageButton  = findViewById(R.id.btnRed)
        val btnGreen :ImageButton  = findViewById(R.id.btnGreen)
        val btnBlue :ImageButton  = findViewById(R.id.btnBlue)
        val btnBlack :ImageButton  = findViewById(R.id.btnBlack)
        val btnClear :ImageButton  = findViewById(R.id.btnClear)
        val btSize :ImageButton  = findViewById(R.id.btnSize)

        val btnUndo :ImageButton  = findViewById(R.id.btnUndo)


        val btnGallery :ImageButton  = findViewById(R.id.btnGallery)
        val btnSave :ImageButton  = findViewById(R.id.btnSave)

        btnRed.setOnClickListener {

            drawingView.setPathColor(Color.RED)
         }
        btnGreen.setOnClickListener {

            drawingView.setPathColor(Color.GREEN)

        }
        btnBlue.setOnClickListener {

            drawingView.setPathColor(Color.BLUE)

        }
        btnBlack.setOnClickListener {

            drawingView.setPathColor(Color.BLACK)

        }
        btSize.setOnClickListener {
            showBrushSizeChooserDialog()

        }
        btnUndo.setOnClickListener {
            drawingView.undoPath()

        }
        btnClear.setOnClickListener {

            drawingView.clearCanvas()

        }

        btnSave.setOnClickListener {

            setupSave(backgroundIv)


        }

        pickMedia = registerForActivityResult(PickVisualMedia()) { uri ->
            uri?.let{ Glide.with(this).load(uri).into(backgroundIv)}
        }

        btnGallery.setOnClickListener {

            setUpBackgroundPicker(backgroundIv)

        }
    }


    private fun showBrushSizeChooserDialog() {
        val brushDialog = Dialog(this)
        brushDialog.setContentView(R.layout.dialog_brush_size)

        val changeSize = brushDialog.findViewById<SeekBar>(R.id.change_size)
        val cancel_button = brushDialog.findViewById<Button>(R.id.cancel_button)
        val ok_button = brushDialog.findViewById<Button>(R.id.ok_button)
        val tvSize = brushDialog.findViewById<TextView>(R.id.textView)
        tvSize.text = drawingView.paint.strokeWidth.toInt().toString()//drawingView.paint.strokeWidth.toString() //drawingView.mBrushSize.toInt().toString() 10 * resources.displayMetrics.density
        changeSize.progress = drawingView.paint.strokeWidth.toInt()//drawingView.mBrushSize.toInt()

        changeSize.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                tvSize.text = seekBar.progress.toString()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                tvSize.text = seekBar.progress.toString()
            }        })

        ok_button.setOnClickListener {

            drawingView.setPathWidth(changeSize.progress)
            brushDialog.dismiss()
        }

        cancel_button.setOnClickListener {

            brushDialog.dismiss()
        }

        brushDialog.show()
    }

    private fun setUpBackgroundPicker(backgroundIv: ImageView){
            pickMedia.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))

    }

    private fun setupSave(backgroundIv: ImageView){

        if(backgroundIv.drawable == null){
            backgroundIv.setBackgroundColor(getColor(R.color.off_white))
        }

            val dialog = inProgress()
            lifecycleScope.launch(Dispatchers.IO){
                val bitmap = findViewById<FrameLayout>(R.id.drawing_fl).drawToBitmap()

                val values = ContentValues().apply {
                    put(
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        System.currentTimeMillis().toString().substring(2, 11) + ".jpeg")
                    put(MediaStore.MediaColumns.MIME_TYPE, "img/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM)
                }

                val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    contentResolver.openOutputStream(uri).use { image ->
                        image?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, image) }
                    } }

                dialog.dismiss()

                val shareIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TITLE, "Sharing image")
                    type = "image/jpeg"
                }
                startActivity(Intent.createChooser(shareIntent, null))
            }

    }

    private fun inProgress(): Dialog{
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_progress_bar)
        dialog.setCancelable(false)
        dialog.show()
        return dialog
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home->{
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}

