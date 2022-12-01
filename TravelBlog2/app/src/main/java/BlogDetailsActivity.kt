package com.travelblog

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.travelblog.databinding.ActivityBlogDetailsBinding
import kotlinx.android.synthetic.main.activity_blog_details.*

private const val IMAGE_URL = "https://bitbucket.org/dmytrodanylyk/travel-blog-resources/raw/" +
        "3436e16367c8ec2312a0644bebd2694d484eb047/images/sydney_image.jpg"
private const val AVATAR_URL = "https://bitbucket.org/dmytrodanylyk/travel-blog-resources/raw/" +
        "3436e16367c8ec2312a0644bebd2694d484eb047/avatars/avatar1.jpg"

class BlogDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBlogDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding = ActivityBlogDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadData()

        //Uses Glide online image library
//        Glide.with(this)
//            .load(IMAGE_URL)
//            .transition(DrawableTransitionOptions.withCrossFade())
//            .into(imageMain)
//
//        Glide.with(this)
//            .load(AVATAR_URL)
//            .transform(CircleCrop())
//            .transition(DrawableTransitionOptions.withCrossFade())
//            .into(imageAvatar)

//        binding.imageMain.setImageResource(R.drawable.sydney)
//        binding.imageAvatar.setImageResource(R.drawable.happy_man)
//
//        binding.textTitle.text = "G'day from Sydney"
//        binding.textDate.text = "August 2, 2019"
//        binding.textAuthor.text = "Grayson Wells"
//        binding.textRating.text = "4.4"
//        binding.textViews.text = "(2687 views)"
//        binding.textDescription.text = "Australia is one of the most popular travel destinations in the world."
//
//        binding.ratingBar.rating = 4.4f

        binding.imageBack.setOnClickListener { finish() }
    }

    private fun loadData() {
        BlogHttpClient.loadBlogArticles(
            onSuccess = { list: List<BlogModel.Blog> ->
                runOnUiThread { showData(list[0]) }
            },
            onError = {

            }
        )
    }

    private fun showData(blog: BlogModel.Blog) {
        binding.textTitle.text = blog.title
        binding.textDate.text = blog.date
        binding.textAuthor.text = blog.author.name
        binding.textRating.text = blog.rating.toString()
        binding.textViews.text = String.format("(%d views)", blog.views)
        binding.textDescription.text = blog.description
        binding.ratingBar.rating = blog.rating

        Glide.with(this)
            .load(blog.image)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imageMain)
        Glide.with(this)
            .load(blog.author.avatar)
            .transform(CircleCrop())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.imageAvatar)
    }
}