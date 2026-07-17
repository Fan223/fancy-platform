package fan.fancy.server.authorization.service.impl;

import fan.fancy.api.auth.pojo.enums.IdentityType;
import fan.fancy.server.authorization.mapper.UserIdentityMapper;
import fan.fancy.server.authorization.pojo.entity.UserIdentityDO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserIdentityServiceImplTest {

    @Mock
    private UserIdentityMapper userIdentityMapper;

    @InjectMocks
    private UserIdentityServiceImpl userIdentityService;

    @Test
    void getByIdentifier_returnsMapperResult() {
        UserIdentityDO expected = new UserIdentityDO();
        expected.setId(1L);
        when(userIdentityMapper.selectByIdentifier(anyInt(), anyString())).thenReturn(expected);

        UserIdentityDO actual = userIdentityService.getByIdentifier(IdentityType.USERNAME, "fan");

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void getByIdentifier_returnsNullWhenNotFound() {
        when(userIdentityMapper.selectByIdentifier(anyInt(), anyString())).thenReturn(null);

        UserIdentityDO actual = userIdentityService.getByIdentifier(IdentityType.USERNAME, "missing");

        assertThat(actual).isNull();
    }
}