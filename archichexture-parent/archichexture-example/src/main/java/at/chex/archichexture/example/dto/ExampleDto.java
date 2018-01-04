package at.chex.archichexture.example.dto;

import at.chex.archichexture.dto.BaseDto;
import at.chex.archichexture.example.model.Example;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import javax.ws.rs.FormParam;

/**
 * @author cheX GmbH Austria {@literal chex@chex.at}
 * @author Jakob Galbavy {@literal jg@chex.at}
 * @since 03.01.18
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ExampleDto extends BaseDto<Example> {

  @FormParam("title")
  @JsonProperty("title")
  private String title;
  @FormParam("ever")
  @JsonProperty("ever")
  private String ever;
  @FormParam("blubber")
  @JsonProperty("blubber")
  private String blubber;

  public ExampleDto() {

  }

  public ExampleDto(Example example) {
    super(example);
    this.title = example.getTitle();
    this.ever = example.getWhatever();
    this.blubber = example.getBlub();
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("title", title)
        .add("ever", ever)
        .add("blubber", blubber)
        .add("id", id)
        .toString();
  }
}
