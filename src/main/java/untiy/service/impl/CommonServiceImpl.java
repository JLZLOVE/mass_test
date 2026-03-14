package untiy.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import untiy.mapper.CommonMapper;
import untiy.service.CommonService;


import java.util.HashMap;
import java.util.List;

@Service
public class CommonServiceImpl implements CommonService {
    @Autowired
    CommonMapper commonMapper;
    @Override
    public List<String> requireOption(HashMap<String, Object> map) {
List<String> list=commonMapper.requireMapper(map);
        return list;
    }
}
