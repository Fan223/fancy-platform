package fan.fancy.datasource.provider;

import fan.fancy.datasource.model.DataSourceModel;

import java.util.List;

/**
 * 数据源提供者接口.
 *
 * @author Fan
 */
public interface DataSourceProvider {

    List<DataSourceModel> load();
}
