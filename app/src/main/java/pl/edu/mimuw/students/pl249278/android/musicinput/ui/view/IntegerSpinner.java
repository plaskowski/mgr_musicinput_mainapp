package pl.edu.mimuw.students.pl249278.android.musicinput.ui.view;

import android.view.View;

public class IntegerSpinner {
	
	public static abstract class IntegerSpinnerModel {
		private Integer minValue, maxValue;
		private Integer value;

		protected abstract Integer prevValue(int value);
		protected abstract Integer nextValue(int value);
		
		public void nextValue() {
			setValue(nextValue(getValue()));
		}
		public boolean hasNext() {
			return maxValue == null || nextValue(getValue()) <= maxValue;
		}
		
		public void prevValue() {
			if(hasPrev())
				setValue(prevValue(getValue()));
		}
		public boolean hasPrev() {
			return minValue == null || prevValue(getValue()) >= minValue;
		}
		
		/**
		 * @return self
		 */
		public IntegerSpinnerModel setMinValue(Integer minValue) {
			this.minValue = minValue;
			filterValue();
			return this;
		}
		
		/**
		 * @return 
		 * @return self
		 */
		public IntegerSpinnerModel setMaxValue(Integer maxValue) {
			this.maxValue = maxValue;
			filterValue();
			return this;
		}
		
		private void filterValue() {
			if(value == null)
				return;
			if(minValue != null)
				setRawValue(Math.max(getValue(), minValue));
			if(maxValue != null)
				setRawValue(Math.min(getValue(), maxValue));
		}
		
		/**
		 * @return self
		 */
		public IntegerSpinnerModel setValue(int value) {
			setRawValue(value);
			filterValue();
			return this;
		}
		
		private void setRawValue(int value) {
			this.value = value;
		}
		public Integer getValue() {
			return value;
		}
	}	
	
	public static class MultiplyModel extends IntegerSpinnerModel {
		private int factor;

		public MultiplyModel(int factor) {
			this.factor = factor;
		}

		@Override
		protected Integer prevValue(int value) {
			return value/factor;
		}

		@Override
		protected Integer nextValue(int value) {
			return value*factor;
		}
		
	}
	
	public static class IncrementModel extends IntegerSpinnerModel {
		private int step;

		public IncrementModel(int step) {
			this.step = step;
		}

		@Override
		protected Integer prevValue(int value) {
			return value-step;
		}

		@Override
		protected Integer nextValue(int value) {
			return value+step;
		}
	}

	public static class IntegerSpinnerController implements View.OnClickListener {
		private View less, more;
		protected IntegerSpinnerModel model;

		public IntegerSpinnerController(IntegerSpinnerModel model, View wrapper, int incrementButtonId, int decrementButtonId) {
			this.model = model;
			less = wrapper.findViewById(decrementButtonId);
			less.setOnClickListener(this);
			more = wrapper.findViewById(incrementButtonId);
			more.setOnClickListener(this);
			updateViews();
		}

		public int getValue() {
			return model.getValue();
		}

		public void setValue(int value) {
			model.setValue(value);
			updateViews();
		}

		@Override
		public void onClick(View v) {
			if(v == less) {
				model.prevValue();
			} else if(v == more) {
				model.nextValue();
			} else {
				throw new RuntimeException();
			}
			updateViews();
		}

		protected void updateViews() {
			less.setEnabled(model.hasPrev());
			more.setEnabled(model.hasNext());
		}

	}	
}
