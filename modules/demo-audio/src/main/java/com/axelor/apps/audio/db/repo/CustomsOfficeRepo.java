package com.axelor.apps.audio.db.repo;

import com.axelor.apps.audio.db.CustomsOffice;

import java.util.List;

public class CustomsOfficeRepo extends CustomsOfficeRepository{

    public List<CustomsOffice> findAllParent() {
        return all().filter("self.parent = null").fetch();
    }

    public List<CustomsOffice> findAllByParentId(Long parentId) {
        return all().filter("self.parent.id = :parentId")
                .bind("parentId", parentId)
                .fetch();
    }

}
