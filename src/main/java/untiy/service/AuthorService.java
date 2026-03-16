package untiy.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.security.core.GrantedAuthority;
import untiy.entity.ClubStatistics;

import java.util.List;

public interface AuthorService  {
    public List<GrantedAuthority> getAuthoritiesByUserId(Long userId);
}
