package untiy.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 社团成员数批量聚合结果。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberCountVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long clubId;

    private Integer memberCount;
}
