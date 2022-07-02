package com.wdhcr.minioupload.controller;

import com.wdhcr.minioupload.component.MinioComp;
import com.wdhcr.minioupload.domain.R;
import io.minio.StatObjectResponse;
import io.minio.http.Method;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class UploadController {

    @Autowired
    private MinioComp minioComp;

    /**
     * 上传文件
     *
     * @param file     文件
     * @param fileName 文件名
     * @return
     */
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile file, @RequestParam("fileName") String fileName) {
        if(file == null){
            return R.error("上传文件不能为空");
        }
        minioComp.upload(file, fileName);
        //文件上传成功后，返回一个可访问的URL
        String url = minioComp.getUrl(fileName, 7, TimeUnit.DAYS);
        return R.success(url);
    }

    /**
     * 前端formData直传
     *
     * @param fileName
     * @return
     */
    @GetMapping("/policy")
    public R policy(@RequestParam("fileName") String fileName) {
        Map policy = minioComp.getPolicy(fileName, ZonedDateTime.now().plusMinutes(10));
        return R.success(policy);
    }

    /**
     * 前端Url直传
     *
     * @param fileName
     * @return
     */
    @GetMapping("/uploadUrl")
    public R uploadUrl(@RequestParam("fileName") String fileName) {
        String url = minioComp.getPolicyUrl(fileName, Method.PUT, 2, TimeUnit.MINUTES);
        return R.success(url);
    }

    /**
     * 获取文件访问地址
     *
     * @param fileName 文件名
     * @return
     */
    @GetMapping("/url")
    public R getUrl(@RequestParam("fileName") String fileName) {
        String url = minioComp.getUrl(fileName, 7, TimeUnit.DAYS);
        return R.success(url);
    }

    /**
     * 下载文件
     *
     * @param fileName 文件名
     * @param response
     */
    @GetMapping(value = "/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        InputStream in = null;
        try {
        final StatObjectResponse stat = minioComp.getStatObject(fileName);
        response.setContentType(stat.contentType());
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8"));
        in = minioComp.getObject(fileName);
        IOUtils.copy(in, response.getOutputStream());
        }catch (Exception e){
            e.getMessage();
        }finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.getMessage();
                }
            }
        }
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     * @return
     */
    @DeleteMapping("/delete")
    public R delete(@RequestParam("fileName") String fileName) {
        minioComp.remove(fileName);
        return R.success();
    }

}
