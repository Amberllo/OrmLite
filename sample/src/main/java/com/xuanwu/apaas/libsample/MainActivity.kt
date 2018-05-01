package com.xuanwu.apaas.libsample

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import com.xuanwu.apaas.libsample.store.*
import com.xuanwu.apaas.ormlib.core.SqliteOrmLite

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.util.*

class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setTitle("test")

        var users = UserRepository().findAll()
        var adapter = UserAdapter(this,users)
        listView.adapter = adapter
        listView.onItemClickListener = AdapterView.OnItemClickListener {
            adapterView: AdapterView<*>, view: View, position: Int, l: Long ->
            var user = users[position]
            var userId = user.userId
            var intent = Intent(this,UserActivity::class.java)
            intent.putExtra("userId",userId)
            startActivity(intent)
        }

        listView.onItemLongClickListener = AdapterView.OnItemLongClickListener {
            adapterView: AdapterView<*>, view: View, position: Int, l: Long ->
            var user = users[position]
            var userId = user.userId
            UserRepository().deleteById(userId)


            users.clear()
            users.addAll(UserRepository().findAll())
            adapter.notifyDataSetChanged()

            true
        }
        adapter.notifyDataSetChanged()

        fab.setOnClickListener({ view ->

            var bizModelList = TestString().get(this)

            var list1 = bizModelList.subList(3,4)
//            var list2 = bizModelList.subList(0,10)
//            var list3 = bizModelList.subList(0,10)
//            var list4 = bizModelList.subList(0,10)
//            var list5 = bizModelList.subList(0,10)
            BizModelRepository("TestDB").saveOrUpdate(list1)
//            BizModelRepository("TestDB").saveOrUpdate(list2)
//            BizModelRepository("TestDB").saveOrUpdate(list3)
//            BizModelRepository("TestDB").saveOrUpdate(list4)
//            BizModelRepository("TestDB").saveOrUpdate(list5)




            var time = System.currentTimeMillis()
            var list = ArrayList<UserBean>()
//            for(i in 0..100){
//                var user1 = UserBean()
//                user1.userId = "UUID_"+i
//                user1.userName = "用户名"
//                user1.telephone = "+8615812658162"
//                list.add(user1)
//            }
            var stp1 = System.currentTimeMillis()
            println("create Object:"+( stp1  - time))

            UserRepository().saveOrUpdate(list)
            var stp2 = System.currentTimeMillis()
            println("save DB:"+( stp2  - stp1))

            users.clear()
            users.addAll(UserRepository().findAll())
            adapter.notifyDataSetChanged()
            var stp3 = System.currentTimeMillis()
            println("findAll Object:"+( stp3  - stp2))




//            for(i in 0..100){
//
//
//                list.add(user1)
//            }

            var area = AreaBean()
            area.parentId = "140000"
            area.regionId = "140800"
            area.idpath = "140000.140800"
            AreaRepository().saveOrUpdate(area)


        })
    }

    @SuppressLint("MissingSuperCall")
    override fun onDestroy() {
        super.onDestroy()
        SqliteOrmLite.onDestory();
        System.exit(0)
    }
}
