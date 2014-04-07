package edu.dartmouth.cs.myruns5;

public class Person {
	public static enum Gender {
		MALE, FEMALE
	}
	// Head apparel for covering face. Numbers represent % of total body surface area covered (ie: face)
	public static enum HeadApparelType {NONE(0f), BASEBALLCAP(2.5f);
		
		float cover;
		HeadApparelType(float cover) {
			this.cover = cover;
		}

		public float getCover() {
			return cover;
		}
		
		public enum LowerApparelMaleType {
			NONE(0f), SHORTS(0.25f), JEANS(0.5f);
			
			float cover;
			LowerApparelMaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}
		
		public enum LowerApparelFemaleType {
			NONE(0f), BIKINI_BOTTOM(.1f), SHORTS(0.23f), JEANS(0.5f);
			
			float cover;
			LowerApparelFemaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}
		
		public enum UpperApparelMaleType {
			NONE(0f), TANK_TOP(0.20f),TEESHIRT_SHORTSLEEVE(0.25f), TEESHIRT_LONGSLEEVE(0.35f);

			float cover;
			UpperApparelMaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}

		public enum UpperApparelFemaleType {
			NONE(0f), BIKINI_TOP(.1f),SPORTS_BRA(0.05f),TANK_TOP(0.20f),TEESHIRT_SHORTSLEEVE(0.25f),TEESHIRT_LONGSLEEVE(0.35f);

			float cover;
			UpperApparelFemaleType(float cover) {
				this.cover = cover;
			}

			public float getCover() {
				return cover;
			}
		}
	}
}
