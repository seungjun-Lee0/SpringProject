package lsj.spring.mvc.service;

import lsj.spring.mvc.dao.GalleryDAO;
import lsj.spring.mvc.utils.ImgUploadUtil;
import lsj.spring.mvc.vo.Gallery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service("gsrv")
public class GalleryServiceImpl implements GalleryService{

    @Autowired private GalleryDAO gdao;
    @Autowired private ImgUploadUtil imgutil;

    @Override // 이미지 업로드 후 디비에 저장
    public boolean newGal(Gallery g, MultipartFile[] img) {

        String uuid = imgutil.makeUUID(); // 식별코드 생성

        // 첨부파일이 존재한다면
        if(imgutil.checkGalleryFiles(img)){


            // 업로드한 이미지파일명을 저장하기 위해 동적배열 생성
            List<String> imgs = new ArrayList<>();

            // 첨부파일이 존재한다면 서버에 저장하고
            // 그 결과로 파일이름을 받아서 동적배열에 저장
            for(MultipartFile f : img) {
                if (!f.getOriginalFilename().isEmpty())
                    imgs.add(imgutil.ImageUpload(f, uuid));
                    // 업로드한 뒤 결과값은 '파일명/파일크기'로 넘어옴
                else
                    imgs.add("-/-");
                // 업로드를 하지 못한 경우는 '-/-'만 넘김
            }

            // 결과값을 테이블에 저장하기 위해
            // 파일명과 파일 크기별로 분리해서
            // 단일변수에 저장함
            String fnames = "";
            String fsizes = "";

            for (int i = 0; i < imgs.size(); ++i) {
                fnames += imgs.get(i).split("[/]")[0] + "/";
                fsizes += imgs.get(i).split("[/]")[1] + "/";
            }

            g.setFnames( fnames );
            g.setFsizes( fsizes );
            g.setUuid( uuid );
        }

        // 업로드한 이미지 정보를 테이블에 저장
        int id = gdao.insertGal(g);

        // 업로드한 이미지들 중 첫번쨰 이미지를 썸내일로 만듬
        String ofname = g.getFnames().split("/")[0];
        int pos = ofname.lastIndexOf(".");
        String fname = ofname.substring(0, pos);
        String fext = ofname.substring(pos+1);
        ofname = fname + uuid + "." + fext;

        imgutil.imageCropResize(ofname, id + "");

        return true;
    }

    @Override
    public List<Gallery> readGal(String cp) {
        int snum = (Integer.parseInt(cp) - 1) * 10;

        return gdao.selectGal(snum);
    }

    @Override
    public Gallery readOneGal(String gno) {
        return gdao.selectOneGal(gno);
    }
}