服务功能：  （压缩完之后暂未添加删除操作）
    对录音文件进行压缩迁移，执行以下压缩命令：
     eg: sox /home/video-compress-test/nas/2020/02/31/6d5d4641-8a79-4786-81b3-a64a13ec8dd9.wav -r 8000 -c 1 -e gsm-full-rate /home/video-compress-test/nas-bak/2020/02/31/6d5d4641-8a79-4786-81b3-a64a13ec8dd9.wav
服务部署与使用说明：
    选择一台服务进行部署，最好该服务器上无业务，防止压缩线程产生影响，
    由于录音文件数量较大，故写了对应接口以便灵活处理：
部署前置；
    需要获取到录音源文件的目录：eg: /home/nas/
    新建压缩之后的，录音文件存储的文件夹，eg:  /home/nas-bak/
    配置文件：
        auto.compress   值改为0，代表手动调用，后期可使用定时任务跑前一个月的
        auto.time        自动执行的时间，后期可调为每月1号 
        nas.srcPath      录音源文件的目录   eg: /home/record/
        nas.bakPath      压缩之后的录音存储目录   eg: /home/record1/
使用：
    手动调用接口：
        1 desc:  压缩指定年月下所有的录音文件：
              eg:   address: http://ip:8888/manual?year=2020&month=01   压缩1月份所有的录音文件
        2 desc:  压缩指定日期范围内下所有的录音文件：
              eg:   address: http://ip:8888/manual/range?year=2020&month=01&range=01   压缩2020年01月01号的一天的录音文件
              eg:   address: http://ip:8888/manual/range?year=2020&month=01&range=01-05   压缩2020年01月01号到05号的5天的录音文件
        3 desc   执行上个月的压缩任务：
              eg:   address: http://ip:8888/auto   压缩上个月的录音文件，如当前月是10月，那该接口会执行9月所有的压缩任务
    自动执行：
        需改写该配置文件： auto.compress  值为1  ，并修改cron执行时间表达式
